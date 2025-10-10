package kr.ac.dankook.ace.healthy_meal_backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.transaction.Transactional;
import kr.ac.dankook.ace.healthy_meal_backend.action.MealInfoAction;
import kr.ac.dankook.ace.healthy_meal_backend.dto.DailyIntakeDTO;
import kr.ac.dankook.ace.healthy_meal_backend.dto.MealInfoPostDTO;
import kr.ac.dankook.ace.healthy_meal_backend.dto.UserGetDTO;
import kr.ac.dankook.ace.healthy_meal_backend.entity.DailyIntake;
import kr.ac.dankook.ace.healthy_meal_backend.entity.MealInfo;
import kr.ac.dankook.ace.healthy_meal_backend.entity.User;
import kr.ac.dankook.ace.healthy_meal_backend.repository.DailyIntakeRepository;
import kr.ac.dankook.ace.healthy_meal_backend.repository.MealInfoRepository;
import kr.ac.dankook.ace.healthy_meal_backend.repository.UserRepository;
import kr.ac.dankook.ace.healthy_meal_backend.security.CustomUserDetails;
import kr.ac.dankook.ace.healthy_meal_backend.service.NutrientIntakeService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
@Tag(name = "유저")
public class UserController {

    private final UserRepository userRepository;
    private final MealInfoRepository mealInfoRepository;
    private final DailyIntakeRepository dailyIntakeRepository;
    private final ModelMapper modelMapper;
    private final MealInfoAction mealInfoAction;
    private final NutrientIntakeService nutrientIntakeService;

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @GetMapping("/{userId}")
    @Operation(summary = "주어진 ID를 가진 특정 유저 가져오기", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<UserGetDTO> getUser(@PathVariable String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다: " + userId));
        UserGetDTO userGetDTO = modelMapper.map(user, UserGetDTO.class);
        return ResponseEntity.ok().body(userGetDTO);
    }

    @DeleteMapping("/{userId}")
    @Operation(summary = "주어진 ID를 가진 유저 삭제", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<Object> deleteUser(@PathVariable String userId) {
        if (!userRepository.existsById(userId)) {
            throw new NoSuchElementException("사용자를 찾을 수 없습니다: " + userId);
        }
        userRepository.deleteById(userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{userId}/meal-info")
    @Operation(summary = "주어진 ID의 유저가 기록한 모든 식단 정보 가져오기", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<List<MealInfoPostDTO>> getMealInfo(
            @PathVariable String userId,
            @RequestParam(value = "date", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date
    ) {
        if (!userRepository.existsById(userId)) {
            throw new NoSuchElementException("사용자를 찾을 수 없습니다: " + userId);
        }
        List<MealInfo> mealInfos = mealInfoRepository.findByUserIdAndCreatedDate(userId, date);
        List<MealInfoPostDTO> mealInfoPostDTOs = mealInfos.stream()
                .map(mealInfo -> modelMapper.map(mealInfo, MealInfoPostDTO.class)).toList();
        return ResponseEntity.ok().body(mealInfoPostDTOs);
    }

    @PostMapping(value = "/{userId}/meal-info", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "주어진 정보로 주어진 ID의 유저가 식단정보 기록", security = @SecurityRequirement(name = "BearerAuth"))
    @Transactional
    public ResponseEntity<MealInfoPostDTO> createMealInfo(
            @PathVariable String userId,
            @RequestPart("img") MultipartFile file
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다: " + userId));
        logger.info("식단사진저장 <시작>");
        MealInfo mealInfo = mealInfoAction.createMealInfo(file, user);
        MealInfoPostDTO mealInfoPostDTO = modelMapper.map(mealInfo, MealInfoPostDTO.class);
        logger.info("식단사진저장 <완료>");
        return ResponseEntity.status(HttpStatus.CREATED).body(mealInfoPostDTO);
    }

    @PostMapping("/{userId}/meal-info/{mealInfoId}/analyze")
    @Operation(
            summary = "주어진 ID의 유저가 기록한 주어진 ID의 식단 정보를 gpt가 분석",
            description = "식단 정보와 음식을 연결", security = @SecurityRequirement(name = "BearerAuth"))
    @Transactional
    public ResponseEntity<List<String>> analyzeMealInfo(@PathVariable String userId,
                                                        @PathVariable Long mealInfoId) {

        List<String> foodResult = mealInfoAction.analyzeMealInfo(mealInfoId, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(foodResult);
    }

    @PatchMapping("/{userId}/meal-info/{mealInfoId}")
    @Operation(summary = "주어진 ID의 유저가 기록한 주어진 ID의 식단 정보 최종기록 / 수정", security = @SecurityRequirement(name = "BearerAuth"))
    @Transactional
    public ResponseEntity<MealInfoPostDTO> updateMealInfo(
            @PathVariable String userId, @PathVariable Long mealInfoId,
            @RequestParam(required = false) Float amount, @RequestParam(required = false) String diary,
            @AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody List<String> confirmedFoods) {
        if (amount == null && diary == null) {
            throw new IllegalArgumentException("amount 또는 diary 중 하나는 필수입니다.");
        }

        MealInfo updatedMealInfo = mealInfoAction.completeMealInfo(userDetails.getUser(), mealInfoId, amount, diary, confirmedFoods);
        MealInfoPostDTO mealInfoPostDTO = modelMapper.map(updatedMealInfo, MealInfoPostDTO.class);
        return ResponseEntity.ok(mealInfoPostDTO);
    }

    @GetMapping("/{userId}/meal-info/{mealInfoId}")
    @Operation(summary = "주어진 ID의 유저가 기록한 주어진 ID의 식단 정보 가져오기", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<MealInfoPostDTO> getMealInfo(
            @PathVariable String userId, @PathVariable Long mealInfoId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다: " + userId));
        MealInfo mealInfo = user.getMealInfos().stream()
                .filter(mf -> Objects.equals(mf.getId(), mealInfoId))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("식단 정보를 찾을 수 없습니다: " + mealInfoId));

        return ResponseEntity.ok(modelMapper.map(mealInfo, MealInfoPostDTO.class));
    }

    @DeleteMapping("/{userId}/meal-info/{mealInfoId}")
    @Operation(summary = "주어진 ID의 유저가 기록한 주어진 ID의 식단 정보 삭제", security = @SecurityRequirement(name = "BearerAuth"))
    @Transactional
    public ResponseEntity<Object> deleteMealInfo(
            @PathVariable String userId, @PathVariable Long mealInfoId) {
        if (!userRepository.existsById(userId)) {
            throw new NoSuchElementException("사용자를 찾을 수 없습니다: " + userId);
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다: " + userId));
        mealInfoAction.deleteMealInfo(mealInfoId, user);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{userId}/daily-intake")
    @Operation(summary = "주어진 ID의 유저의 모든 일별섭취기록 가져오기", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<List<DailyIntakeDTO>> getDailyIntakeByUserId(@PathVariable String userId) {
        if (!userRepository.existsById(userId)) {
            throw new NoSuchElementException("사용자를 찾을 수 없습니다: " + userId);
        }
        List<DailyIntake> dailyIntakes = nutrientIntakeService.getDailyIntakes(userId);
        List<DailyIntakeDTO> dailyIntakeDTOs = dailyIntakes.stream()
                .map(dailyIntake -> modelMapper.map(dailyIntake, DailyIntakeDTO.class)).toList();

        return ResponseEntity.ok(dailyIntakeDTOs);
    }

    @GetMapping("/{userId}/daily-intake/{dailyIntakeId}")
    @Operation(summary = "주어진 ID의 유저의 주어진 ID의 일별섭취기록 가져오기", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<DailyIntakeDTO> getDailyIntake(
            @PathVariable String userId,
            @PathVariable Integer dailyIntakeId) {
        DailyIntake dailyIntake = dailyIntakeRepository.findById(dailyIntakeId)
                .orElseThrow(() -> new NoSuchElementException("유저 " + userId + "의 일별섭취기록을 찾을 수 없습니다: " + dailyIntakeId));
        return ResponseEntity.ok().body(modelMapper.map(dailyIntake, DailyIntakeDTO.class));
    }

    @DeleteMapping("/{userId}/daily-intake/{dailyIntakeId}")
    @Operation(summary = "주어진 ID의 유저가 주어진 ID의 일별섭취기록 삭제", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<?> deleteDailyIntake(@PathVariable String userId, @PathVariable Integer dailyIntakeId) {
        if(!dailyIntakeRepository.existsByUserIdAndId(userId, dailyIntakeId)) {
            throw new NoSuchElementException("유저 " + userId + "의 일별섭취기록을 찾을 수 없습니다: " + dailyIntakeId);
        }
        dailyIntakeRepository.deleteById(dailyIntakeId);
        return ResponseEntity.noContent().build();
    }

}