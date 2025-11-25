package kr.ac.dankook.ace.healthy_meal_backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.ac.dankook.ace.healthy_meal_backend.dto.*;
import kr.ac.dankook.ace.healthy_meal_backend.repository.*;
import kr.ac.dankook.ace.healthy_meal_backend.security.CustomUserDetails;
import kr.ac.dankook.ace.healthy_meal_backend.service.MealInfoFoodAnalyzeService;
import kr.ac.dankook.ace.healthy_meal_backend.service.NutrientIntakeService;
import kr.ac.dankook.ace.healthy_meal_backend.service.NutritionService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users/{userId}")
@Tag(name = "영양소 섭취현황")
public class IntakeController {
    private final UserRepository userRepository;
    private final MealInfoRepository mealInfoRepository;
    private final DailyIntakeRepository dailyIntakeRepository;
    private final NutritionService nutritionService;
    private final FoodRepository foodRepository;
    private final NutrientIntakeService nutrientIntakeService;

    private static final Logger logger = LoggerFactory.getLogger(IntakeController.class);
    private final MealInfoFoodAnalyzeService mealInfoFoodAnalyzeService;

    @GetMapping("/daily-intake")
    @Operation(summary = "사용자 영양소 섭취현황 조회", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<NutrientValuesDTO> getDailyIntake(
            @PathVariable String userId,
            @RequestParam(value = "date", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        String authenticatedUserId = userDetails.getUsername();
        if (!authenticatedUserId.equals(userId)) {
            throw new AccessDeniedException("해당 사용자에 대한 접근 권한이 없습니다");
        }
        return ResponseEntity.ok().body(new NutrientValuesDTO(nutrientIntakeService.getDailyIntake(userId, date)));
    }

    @GetMapping("/daily-intakes")
    @Operation(summary = "사용자 영양소 섭취현황 기간단위 조회", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<DailyIntakeDTO> getDailyIntakes(
            @PathVariable String userId,
            @RequestParam(value = "start_date", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(value = "end_date", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        String authenticatedUserId = userDetails.getUsername();
        if (!authenticatedUserId.equals(userId)) {
            throw new AccessDeniedException("해당 사용자에 대한 접근 권한이 없습니다");
        }
        List<DailyIntakeElement> dailyIntakeElements = new ArrayList<>();
        for(LocalDate date : getDateRange(startDate, endDate)) {
            DailyIntakeElement dailyIntakeElement = new DailyIntakeElement();
            dailyIntakeElement.setDate(date);
            dailyIntakeElement.setNutrientValues(nutrientIntakeService.getDailyIntake(userId, date));
            dailyIntakeElements.add(dailyIntakeElement);
        }
        return ResponseEntity.ok().body(new DailyIntakeDTO(dailyIntakeElements));
    }
    private List<LocalDate> getDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null && endDate == null) {
            return Collections.emptyList();
        }
        if (startDate == null) {
            return List.of(endDate);
        }
        if (endDate == null) {
            return List.of(startDate);
        }
        return startDate.datesUntil(endDate.plusDays(1)).collect(Collectors.toList());
    }

    @GetMapping("/daily-intake/score")
    @Operation(summary = "사용자 영양소 점수 조회", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<NutrientValuesDTO> getDailyIntakeScore(
            @PathVariable String userId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        String authenticatedUserId = userDetails.getUsername();
        if (!authenticatedUserId.equals(userId)) {
            throw new AccessDeniedException("해당 사용자에 대한 접근 권한이 없습니다");
        }
        return ResponseEntity.ok().body(nutrientIntakeService.calcDailyIntakeScore(userId, nutritionService.getDietCriterion(userId).getNutrientValues()));
    }

    @GetMapping("/daily-intake/scores")
    @Operation(summary = "사용자 영양소 점수 기간단위 조회", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<DailyScoresDTO> getDailyIntakeScores(
            @PathVariable String userId,
            @RequestParam(value = "start_date", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(value = "end_date", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        String authenticatedUserId = userDetails.getUsername();
        if (!authenticatedUserId.equals(userId)) {
            throw new AccessDeniedException("해당 사용자에 대한 접근 권한이 없습니다");
        }
        List<DailyScoreElement> dailyScoreElements = new ArrayList<>();
        for(LocalDate date : getDateRange(startDate, endDate)) {
            DailyScoreElement dailyScoreElement = new DailyScoreElement();
            dailyScoreElement.setDate(date);
            dailyScoreElement.setDailyscore(dailyIntakeRepository.findByUserIdAndDay(userId, date).getDailyscore());
            dailyScoreElements.add(dailyScoreElement);
        }
        return ResponseEntity.ok().body(new DailyScoresDTO(dailyScoreElements));
    }

    /*
    @PostMapping(value = "/{userId}/meal-info", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "주어진 정보로 주어진 ID의 유저가 식단정보 기록", security = @SecurityRequirement(name = "BearerAuth"))
    @Transactional
    public ResponseEntity<MealRecordDTO> createMealInfo(
            @PathVariable String userId,
            @RequestPart("img") MultipartFile file
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다: " + userId));
        logger.info("식단사진저장 <시작>");
        MealRecord mealRecord = mealInfoAction.createMealInfo(file, user);
        MealRecordDTO mealRecordDTO = modelMapper.map(mealRecord, MealRecordDTO.class);
        logger.info("식단사진저장 <완료>");
        return ResponseEntity.status(HttpStatus.CREATED).body(mealRecordDTO);
    }

    @PostMapping("/{userId}/meal-info/{mealInfoId}/analyze")
    @Operation(
            summary = "주어진 ID의 유저가 기록한 주어진 ID의 식단 정보를 gpt가 분석",
            description = "식단 정보와 음식을 연결", security = @SecurityRequirement(name = "BearerAuth"))
    @Transactional
    public ResponseEntity<FoodElement> analyzeMealInfo(@PathVariable String userId,
                                                       @PathVariable Long mealInfoId) {
        List<String> foodResult = mealInfoAction.analyzeMealInfo(mealInfoId, userId);
        List<Integer> foodWeight = mealInfoFoodAnalyzeService.getFoodWeight(foodResult);
        FoodElement food = new FoodElement(foodResult, foodWeight);
        return ResponseEntity.status(HttpStatus.CREATED).body(food);
    }

    @PatchMapping("/{userId}/meal-info/{mealInfoId}")
    @Operation(summary = "주어진 ID의 유저가 기록한 주어진 ID의 식단 정보 기록/수정", security = @SecurityRequirement(name = "BearerAuth"))
    @Transactional
    public ResponseEntity<MealRecordDTO> updateMealInfo(
            @PathVariable String userId, @PathVariable Long mealInfoId,
            @RequestBody UpdateMealInfoRequestDTO updateMealInfoRequestDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다: " + userId));
        MealRecord mealRecord = user.getMealRecords().stream()
                .filter(mf -> Objects.equals(mf.getId(), mealInfoId))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("식단 정보를 찾을 수 없습니다: " + mealInfoId));
        if(updateMealInfoRequestDTO.getIntakeAmounts().size() != updateMealInfoRequestDTO.getConfirmedFoods().size()) {
            throw new IllegalArgumentException("섭취량과 음식 리스트 길이가 맞지 않음");
        }

        mealRecord.getFoods().clear();

        for(var i=0; i < updateMealInfoRequestDTO.getConfirmedFoods().size(); i++) {
            String foodName = updateMealInfoRequestDTO.getConfirmedFoods().get(i);
            kr.ac.dankook.ace.healthy_meal_backend.entity.Food food = foodRepository.findFirstByName(foodName)
                    .orElseThrow(() -> new IllegalArgumentException(foodName + " 에 해당하는 음식이 없음"));
            mealRecord.addFoodLink(food, updateMealInfoRequestDTO.getIntakeAmounts().get(i));
        }

        mealRecord.setDiary(updateMealInfoRequestDTO.getDiary());

        // 섭취 식단에 따른 영양소 섭취량 계산 -> DailyIntake Update
        nutrientIntakeService.applyInsertDailyIntake(mealRecord, user);

        MealRecordDTO mealRecordDTO = modelMapper.map(mealRecord, MealRecordDTO.class);
        return ResponseEntity.ok(mealRecordDTO);
    }

    @GetMapping("/{userId}/meal-info/{mealInfoId}")
    @Operation(summary = "주어진 ID의 유저가 기록한 주어진 ID의 식단 정보 가져오기", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<MealRecordDTO> getMealInfo(
            @PathVariable String userId, @PathVariable Long mealInfoId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다: " + userId));
        MealRecord mealRecord = user.getMealRecords().stream()
                .filter(mf -> Objects.equals(mf.getId(), mealInfoId))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("식단 정보를 찾을 수 없습니다: " + mealInfoId));
        return ResponseEntity.ok(modelMapper.map(mealRecord, MealRecordDTO.class));
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

    @DeleteMapping("/{userId}/daily-intake/{dailyIntakeId}")
    @Operation(summary = "주어진 ID의 유저가 주어진 ID의 일별섭취기록 삭제", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<?> deleteDailyIntake(@PathVariable String userId, @PathVariable Integer dailyIntakeId) {
        if(!dailyIntakeRepository.existsByUserIdAndId(userId, dailyIntakeId)) {
            throw new NoSuchElementException("유저 " + userId + "의 일별섭취기록을 찾을 수 없습니다: " + dailyIntakeId);
        }
        dailyIntakeRepository.deleteById(dailyIntakeId);
        return ResponseEntity.noContent().build();
    }*/

}