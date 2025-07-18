package kr.ac.dankook.ace.healthy_meal_backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.transaction.Transactional;
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
import kr.ac.dankook.ace.healthy_meal_backend.service.DietaryScoreService;
import kr.ac.dankook.ace.healthy_meal_backend.service.MealInfoFoodAnalyzeService;
import kr.ac.dankook.ace.healthy_meal_backend.service.StorageService;
import org.modelmapper.ModelMapper;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

    public UserController(
            UserRepository userRepository,
            MealInfoRepository mealInfoRepository,
            FoodRepository foodRepository,
            DailyIntakeRepository dailyIntakeRepository,
            StorageService storageService,
            ModelMapper modelMapper,
            MealInfoFoodAnalyzeService mealInfoFoodAnalyzeService,
            FoodController foodController,
            DietaryScoreService dietaryScoreService
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
            @RequestPart("img") MultipartFile file
    ) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        String fileName = storageService.store(file);

        MealInfo mealInfo = new MealInfo();
        mealInfo.setImgPath(fileName);
        mealInfo.setUser(user.get());

        mealInfoRepository.save(mealInfo);
        mealInfo = mealInfoRepository.findById(mealInfo.getId()).get();
        user.get().addMealInfo(mealInfo);

        MealInfoPostDTO mealInfoPostDTO = modelMapper.map(mealInfo, MealInfoPostDTO.class);

        return ResponseEntity.status(HttpStatus.CREATED).body(mealInfoPostDTO);
    }

    @GetMapping("/{userId}/meal-info/{mealInfoId}")
    @Operation(summary = "주어진 ID의 유저가 기록한 주어진 ID의 식단 정보 가져오기", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<MealInfoPostDTO> getMealInfo(@PathVariable String userId, @PathVariable Long mealInfoId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        Optional<MealInfo> mealInfo = user.get().getMealInfos().stream()
                .filter(mf -> Objects.equals(mf.getId(), mealInfoId)).findFirst();
        return mealInfo
                .map(info -> ResponseEntity.status(HttpStatus.OK).body(modelMapper.map(info, MealInfoPostDTO.class)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PatchMapping("/{userId}/meal-info/{mealInfoId}")
    @Operation(summary = "주어진 ID의 유저가 기록한 주어진 ID의 식단 정보 수정", security = @SecurityRequirement(name = "BearerAuth"))
    @Transactional
    public ResponseEntity<MealInfoPostDTO> updateMealInfo(
            @PathVariable String userId, @PathVariable Long mealInfoId,
            @RequestParam(required = false) Float amount, @RequestParam(required = false) String diary) {
        // 사용자 존재 확인
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        // 식단 정보 존재 확인
        Optional<MealInfo> mealInfoOpt = mealInfoRepository.findById(mealInfoId);
        if (mealInfoOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        MealInfo mealInfo = mealInfoOpt.get();

        // 해당 사용자의 식단 정보인지 확인
        if (!mealInfo.getUser().getId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // 수정할 데이터가 있는지 확인
        if (amount == null && diary == null) {
            return ResponseEntity.badRequest().build();
        }

        // 데이터 업데이트
        if (amount != null) {
            mealInfo.setIntakeAmount(amount);
        }
        if (diary != null) {
            mealInfo.setDiary(diary);
        }

        // 저장
        MealInfo updatedMealInfo = mealInfoRepository.save(mealInfo);
        MealInfoPostDTO mealInfoPostDTO = modelMapper.map(updatedMealInfo, MealInfoPostDTO.class);

        // 일별 섭취 기록 업데이트
        updateDailyIntake(mealInfo.getUser().getId(), mealInfo.getCreatedAt().toLocalDate());

        return ResponseEntity.ok(mealInfoPostDTO);
    }

    @DeleteMapping("/{userId}/meal-info/{mealInfoId}")
    @Operation(summary = "주어진 ID의 유저가 기록한 주어진 ID의 식단 정보 삭제", security = @SecurityRequirement(name = "BearerAuth"))
    @Transactional
    public ResponseEntity<Object> deleteMealInfo(@PathVariable String userId, @PathVariable Long mealInfoId) {
        // 사용자 존재 확인
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        // 식단 정보 존재 확인
        Optional<MealInfo> mealInfoOpt = mealInfoRepository.findById(mealInfoId);
        if (mealInfoOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        MealInfo mealInfo = mealInfoOpt.get();

        // 해당 사용자의 식단 정보인지 확인
        if (!mealInfo.getUser().getId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            // 연관된 이미지 파일 삭제 (선택사항)
            if (mealInfo.getImgPath() != null && !mealInfo.getImgPath().isEmpty()) {
                storageService.delete(mealInfo.getImgPath());
            }

            // 사용자의 MealInfo 목록에서 제거
            user.get().getMealInfos().remove(mealInfo);

            // 데이터베이스에서 삭제
            mealInfoRepository.delete(mealInfo);
        } catch (Exception e) {
            // 파일 삭제 실패해도 데이터는 삭제
            mealInfoRepository.delete(mealInfo);
        }

        // 일별 섭취 기록 업데이트
        var dailyIntake = dailyIntakeRepository.findByUserIdAndDay(userId, mealInfo.getCreatedAt().toLocalDate());
        if (!dailyIntake.isEmpty()) {
            deleteDailyIntake(userId, dailyIntake.get(0).getId());
        }
        updateDailyIntake(mealInfo.getUser().getId(), mealInfo.getCreatedAt().toLocalDate());

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping("/{userId}/meal-info/{mealInfoId}/analyze")
    @Operation(
            summary = "주어진 ID의 유저가 기록한 주어진 ID의 식단 정보를 gpt가 분석",
            description = "식단 정보와 음식을 연결", security = @SecurityRequirement(name = "BearerAuth"))
    @Transactional
    public ResponseEntity<List<String>> analyzeMealInfo(@PathVariable String userId, @PathVariable Long mealInfoId) {
        Optional<User> user = userRepository.findById(userId);
        Optional<MealInfo> mealInfo = mealInfoRepository.findById(mealInfoId);
        if (user.isEmpty() || mealInfo.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        String fileName = mealInfo.get().getImgPath();
        List<String> majorCategories = foodRepository.findDistinctMajorCategoryNative();
        List<String> majorCategoriesResult = mealInfoFoodAnalyzeService.analyzeImage(fileName, majorCategories);

        List<String> foodNames = new ArrayList<>();
        for (String majorCategory : majorCategoriesResult) {
            var representativeFoods = foodRepository.findDistinctRepresentativeFoodByMajorCategory(majorCategory);
            var representativeFoodsResult = mealInfoFoodAnalyzeService.analyzeImage(fileName, representativeFoods);
            for (String representativeFood : representativeFoodsResult) {
                var foods = foodRepository.findDistinctNameByRepresentativeFood(representativeFood);
                foodNames.addAll(mealInfoFoodAnalyzeService.analyzeImage(fileName, foods));
            }
        }

        for (String foodName : foodNames) {
            List<Food> foodResult = foodRepository.findAllByName(foodName);
            if (!foodResult.isEmpty())
                foodController.createFoodMealInfoRelation(foodResult.get(0).getId(), mealInfoId);
        }
        if (foodNames.isEmpty())
            foodNames.add("판단 실패!");
        return ResponseEntity.status(HttpStatus.CREATED).body(foodNames);
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

    @PutMapping("/{userId}/daily-intake")
    @Operation(summary = "주어진 ID의 유저가 일별섭취기록 생성/업데이트", security = @SecurityRequirement(name = "BearerAuth"))
    @Transactional
    public ResponseEntity<DailyIntakeDTO> updateDailyIntake(
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
    }

    private DailyIntake createNewDailyIntake(User user, LocalDate date) {
        DailyIntake dailyIntake = new DailyIntake();
        dailyIntake.setUser(user);
        dailyIntake.setDay(date);
        return dailyIntake;
    }

    private void resetNutritionValues(DailyIntake dailyIntake) {
        dailyIntake.setCelluloseG(0f);
        dailyIntake.setFatG(0f);
        dailyIntake.setCarbohydrateG(0f);
        dailyIntake.setProteinG(0f);
        dailyIntake.setCholesterolMg(0f);
        dailyIntake.setEnergyKcal(0);
        dailyIntake.setSugarsG(0f);
        dailyIntake.setSodiumMg(0f);
        dailyIntake.setScore(0);
    }

    private void updateNutritionValues(DailyIntake dailyIntake, List<MealInfo> mealInfos) {
        // 초기화
        resetNutritionValues(dailyIntake);

        // 모든 음식의 영양소 합계 계산
        mealInfos.stream()
                .flatMap(mealInfo -> mealInfo.getFoods().stream())
                .forEach(food -> addFoodNutrition(dailyIntake, food));
    }

    private void addFoodNutrition(DailyIntake dailyIntake, Food food) {
        dailyIntake.setCelluloseG(dailyIntake.getCelluloseG() +
                Optional.ofNullable(food.getCelluloseG()).orElse(0.0).floatValue());
        dailyIntake.setFatG(dailyIntake.getFatG() +
                Optional.ofNullable(food.getFatG()).orElse(0.0).floatValue());
        dailyIntake.setCarbohydrateG(dailyIntake.getCarbohydrateG() +
                Optional.ofNullable(food.getCarbohydrateG()).orElse(0.0).floatValue());
        dailyIntake.setProteinG(dailyIntake.getProteinG() +
                Optional.ofNullable(food.getProteinG()).orElse(0.0).floatValue());
        dailyIntake.setCholesterolMg(dailyIntake.getCholesterolMg() +
                Optional.ofNullable(food.getCholesterolMg()).orElse(0.0).floatValue());
        dailyIntake.setEnergyKcal(dailyIntake.getEnergyKcal() +
                Optional.ofNullable(food.getEnergyKcal()).orElse(0));
        dailyIntake.setSugarsG(dailyIntake.getSugarsG() +
                Optional.ofNullable(food.getSugarsG()).orElse(0.0).floatValue());
        dailyIntake.setSodiumMg(dailyIntake.getSodiumMg() +
                Optional.ofNullable(food.getSodiumMg()).orElse(0.0).floatValue());
    }

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
