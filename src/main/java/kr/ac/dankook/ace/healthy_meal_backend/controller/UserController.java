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
import kr.ac.dankook.ace.healthy_meal_backend.entity.Food;
import kr.ac.dankook.ace.healthy_meal_backend.entity.MealInfo;
import kr.ac.dankook.ace.healthy_meal_backend.entity.User;
import kr.ac.dankook.ace.healthy_meal_backend.repository.DailyIntakeRepository;
import kr.ac.dankook.ace.healthy_meal_backend.repository.FoodRepository;
import kr.ac.dankook.ace.healthy_meal_backend.repository.MealInfoRepository;
import kr.ac.dankook.ace.healthy_meal_backend.repository.UserRepository;
import kr.ac.dankook.ace.healthy_meal_backend.security.CustomUserDetails;
import kr.ac.dankook.ace.healthy_meal_backend.service.DietaryScoreService;
import kr.ac.dankook.ace.healthy_meal_backend.service.MealInfoFoodAnalyzeService;
import kr.ac.dankook.ace.healthy_meal_backend.service.StorageService;
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
@RequestMapping("/users")
@Tag(name = "유저")
public class UserController {
    private final UserRepository userRepository;
    private final MealInfoRepository mealInfoRepository;
    private final FoodRepository foodRepository;
    private final DailyIntakeRepository dailyIntakeRepository;
    private final StorageService storageService;
    private final ModelMapper modelMapper;
    private final MealInfoFoodAnalyzeService mealInfoFoodAnalyzeService;
    private final FoodController foodController;
    private final DietaryScoreService dietaryScoreService;
    private final MealInfoAction mealInfoAction;

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    public UserController(
            UserRepository userRepository,
            MealInfoRepository mealInfoRepository,
            FoodRepository foodRepository,
            DailyIntakeRepository dailyIntakeRepository,
            StorageService storageService,
            ModelMapper modelMapper,
            MealInfoFoodAnalyzeService mealInfoFoodAnalyzeService,
            FoodController foodController,
            DietaryScoreService dietaryScoreService,
            MealInfoAction mealInfoAction
    ) {
        this.userRepository = userRepository;
        this.mealInfoRepository = mealInfoRepository;
        this.foodRepository = foodRepository;
        this.dailyIntakeRepository = dailyIntakeRepository;
        this.storageService = storageService;
        this.modelMapper = modelMapper;
        this.mealInfoFoodAnalyzeService = mealInfoFoodAnalyzeService;
        this.foodController = foodController;
        this.dietaryScoreService = dietaryScoreService;
        this.mealInfoAction = mealInfoAction;
    }

    @GetMapping("/{userId}")
    @Operation(summary = "주어진 ID를 가진 특정 유저 가져오기", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<UserGetDTO> getUser(@PathVariable String userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        UserGetDTO userGetDTO = modelMapper.map(user.get(), UserGetDTO.class);
        return ResponseEntity.status(HttpStatus.OK).body(userGetDTO);
    }

    @DeleteMapping("/{userId}")
    @Operation(summary = "주어진 ID를 가진 유저 삭제", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<Object> deleteUser(@PathVariable String userId) {
        if (!userRepository.existsById(userId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        userRepository.deleteById(userId);
        return ResponseEntity.status(HttpStatus.valueOf(204)).build();
    }

    @GetMapping("/{userId}/meal-info")
    @Operation(summary = "주어진 ID의 유저가 기록한 모든 식단 정보 가져오기", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<List<MealInfoPostDTO>> getMealInfo(
            @PathVariable String userId,
            @RequestParam(value = "date", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date
    ) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        List<MealInfo> mealInfos = mealInfoRepository.findByUserIdAndCreatedDate(userId, date);
        List<MealInfoPostDTO> mealInfoPostDTOs = mealInfos.stream()
                .map(mealInfo -> modelMapper.map(mealInfo, MealInfoPostDTO.class)).toList();
        return ResponseEntity.status(HttpStatus.OK).body(mealInfoPostDTOs);
    }

    @PostMapping(value = "/{userId}/meal-info", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "주어진 정보로 주어진 ID의 유저가 식단정보 기록", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<MealInfoPostDTO> createMealInfo(
            @PathVariable String userId,
            @RequestPart("img") MultipartFile file,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        // @PathVariable_userId 유효성 검증
        if (!userId.equals(userDetails.getUsername())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        logger.info("식단사진저장 <시작>");
        try {
            MealInfo mealInfo = mealInfoAction.createMealInfo(file, userDetails.getUser());
            MealInfoPostDTO mealInfoPostDTO = modelMapper.map(mealInfo, MealInfoPostDTO.class);
            logger.info("식단사진저장 <완료>");
            return ResponseEntity.status(HttpStatus.CREATED).body(mealInfoPostDTO);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/{userId}/meal-info/{mealInfoId}/analyze")
    @Operation(
            summary = "주어진 ID의 유저가 기록한 주어진 ID의 식단 정보를 gpt가 분석",
            description = "식단 정보와 음식을 연결", security = @SecurityRequirement(name = "BearerAuth"))
    @Transactional
    public ResponseEntity<List<String>> analyzeMealInfo(@PathVariable String userId,
                                                        @PathVariable Long mealInfoId,
                                                        @AuthenticationPrincipal CustomUserDetails userDetails) {
        // @PathVariable_userId 유효성 검증
        if (!userId.equals(userDetails.getUsername())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        List<String> foodResult;
        try {
            foodResult = mealInfoAction.analyzeMealInfo(mealInfoId, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(foodResult);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PatchMapping("/{userId}/meal-info/{mealInfoId}")
    @Operation(summary = "주어진 ID의 유저가 기록한 주어진 ID의 식단 정보 최종기록 / 수정", security = @SecurityRequirement(name = "BearerAuth"))
    @Transactional
    public ResponseEntity<MealInfoPostDTO> updateMealInfo(
            @PathVariable String userId, @PathVariable Long mealInfoId,
            @RequestParam(required = false) Float amount, @RequestParam(required = false) String diary,
            @AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody List<String> confirmedFoods) {
        // @PathVariable_userId 유효성 검증
        if (!userId.equals(userDetails.getUsername())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        // @RequestParam_amount_diary 유효성 검증
        if (amount == null && diary == null) {
            return ResponseEntity.badRequest().build();
        }
        try {
            MealInfo updatedMealInfo = mealInfoAction.completeMealInfo(userDetails.getUser(), mealInfoId, amount, diary, confirmedFoods);
            MealInfoPostDTO mealInfoPostDTO = modelMapper.map(updatedMealInfo, MealInfoPostDTO.class);
            return ResponseEntity.ok(mealInfoPostDTO);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{userId}/meal-info/{mealInfoId}")
    @Operation(summary = "주어진 ID의 유저가 기록한 주어진 ID의 식단 정보 가져오기", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<MealInfoPostDTO> getMealInfo(@PathVariable String userId, @PathVariable Long mealInfoId, @AuthenticationPrincipal CustomUserDetails userDetails) {
        // @PathVariable_userId 유효성 검증
        if (!userId.equals(userDetails.getUsername())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        User user = userDetails.getUser();
        Optional<MealInfo> mealInfo = user.getMealInfos().stream()
                .filter(mf -> Objects.equals(mf.getId(), mealInfoId)).findFirst();
        return mealInfo
                .map(info -> ResponseEntity.status(HttpStatus.OK).body(modelMapper.map(info, MealInfoPostDTO.class)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @DeleteMapping("/{userId}/meal-info/{mealInfoId}")
    @Operation(summary = "주어진 ID의 유저가 기록한 주어진 ID의 식단 정보 삭제", security = @SecurityRequirement(name = "BearerAuth"))
    @Transactional
    public ResponseEntity<Object> deleteMealInfo(
            @PathVariable String userId, @PathVariable Long mealInfoId, @AuthenticationPrincipal CustomUserDetails userDetails) {
        // @PathVariable_userId 유효성 검증
        if (!userId.equals(userDetails.getUsername())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        try {
            mealInfoAction.deleteMealInfo(mealInfoId, userDetails.getUser());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{userId}/daily-intake")
    @Operation(summary = "주어진 ID의 유저의 모든 일별섭취기록 가져오기", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<List<DailyIntakeDTO>> getDailyIntakeByUserId(@PathVariable String userId) {
        if (!userRepository.existsById(userId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        List<DailyIntake> dailyIntakes = dailyIntakeRepository.findByUserId(userId);
        List<DailyIntakeDTO> dailyIntakeDTOs = dailyIntakes.stream()
                .map(dailyIntake -> modelMapper.map(dailyIntake, DailyIntakeDTO.class)).toList();
        return ResponseEntity.status(HttpStatus.OK).body(dailyIntakeDTOs);
    }

    @GetMapping("/{userId}/daily-intake/{dailyIntakeId}")
    @Operation(summary = "주어진 ID의 유저의 주어진 ID의 일별섭취기록 가져오기", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<DailyIntakeDTO> getDailyIntake(@PathVariable String userId, @PathVariable Integer dailyIntakeId) {
        if(!userRepository.existsById(userId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        if(dailyIntakeRepository.existsByUserIdAndId(userId, dailyIntakeId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        DailyIntake dailyIntake = dailyIntakeRepository.findById(dailyIntakeId).get();
        return ResponseEntity.status(HttpStatus.OK).body(modelMapper.map(dailyIntake, DailyIntakeDTO.class));
    }

    /*@PutMapping("/{userId}/daily-intake")
    @Operation(summary = "주어진 ID의 유저가 일별섭취기록 생성/업데이트", security = @SecurityRequirement(name = "BearerAuth"))
    @Transactional
    public ResponseEntity<DailyIntakeDTO> applyInsertDailyIntake(
            @PathVariable String userId,
            @RequestParam(value = "date")
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date
    ) {
        // 사용자 존재 확인
        User user = userRepository.findById(userId)
                .orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        // 해당 날짜의 식단 정보 조회
        List<MealInfo> mealInfos = mealInfoRepository.findByUserIdAndCreatedDate(userId, date);
        if (mealInfos.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        // 기존 기록 조회 또는 새로 생성
        DailyIntake dailyIntake = dailyIntakeRepository.findByUserIdAndDay(userId, date)
                .stream()
                .findFirst()
                .orElse(createNewDailyIntake(user, date));

        boolean isNewRecord = dailyIntake.getId() == null;

        // 영양소 계산 및 업데이트
        updateNutritionValues(dailyIntake, mealInfos);

        // 저장
        DailyIntake savedDailyIntake = dailyIntakeRepository.save(dailyIntake);
        DailyIntakeDTO dailyIntakeDTO = modelMapper.map(savedDailyIntake, DailyIntakeDTO.class);

        HttpStatus status = isNewRecord ? HttpStatus.CREATED : HttpStatus.OK;
        return ResponseEntity.status(status).body(dailyIntakeDTO);
    }*/

    @PutMapping("/{userId}/daily-intake/score")
    @Operation(summary = "주어진 ID의 유저가 주어진 날짜의 일별섭취기록 점수 업데이트", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<DailyIntakeDTO> updateDailyIntakeScore(
            @PathVariable String userId,
            @RequestParam(value = "date")
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        try {
            DailyIntake dailyIntake = dietaryScoreService.calculateScoreFromDailyIntake(userId, date);
            return ResponseEntity.ok(modelMapper.map(dailyIntake, DailyIntakeDTO.class));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @DeleteMapping("/{userId}/daily-intake/{dailyIntakeId}")
    @Operation(summary = "주어진 ID의 유저가 주어진 ID의 일별섭취기록 삭제", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<?> deleteDailyIntake(@PathVariable String userId, @PathVariable Integer dailyIntakeId) {
        if (dailyIntakeRepository.existsByUserIdAndId(userId, dailyIntakeId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        dailyIntakeRepository.deleteById(dailyIntakeId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
