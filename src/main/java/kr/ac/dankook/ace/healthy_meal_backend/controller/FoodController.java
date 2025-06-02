package kr.ac.dankook.ace.healthy_meal_backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.transaction.Transactional;
import kr.ac.dankook.ace.healthy_meal_backend.entity.Food;
import kr.ac.dankook.ace.healthy_meal_backend.entity.MealInfo;
import kr.ac.dankook.ace.healthy_meal_backend.entity.User;
import kr.ac.dankook.ace.healthy_meal_backend.repository.FoodRepository;
import kr.ac.dankook.ace.healthy_meal_backend.repository.MealInfoRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/foods")
@Tag(name = "음식")
public class FoodController {
    private final FoodRepository foodRepository;
    private final MealInfoRepository mealInfoRepository;
    private final ModelMapper modelMapper;

    @Autowired
    public FoodController(final FoodRepository foodRepository, final MealInfoRepository mealInfoRepository, final ModelMapper modelMapper) {
        this.foodRepository = foodRepository;
        this.mealInfoRepository = mealInfoRepository;
        this.modelMapper = modelMapper;
    }

    @GetMapping()
    @Operation(summary = "이름, 대표음식명, 대분류명으로 음식들 가져오기",
            description = "모든 파라미터는 선택적")
    public List<Food> getFood(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String representativeFood,
            @RequestParam(required = false) String majorCategory
    ) {
        return foodRepository.findByDynamicConditions(name, representativeFood, majorCategory);
    }

    @GetMapping("/{foodId}")
    @Operation(summary = "주어진 ID를 가진 특정 음식 가져오기")
    public ResponseEntity<Food> getFoodById(@PathVariable long foodId) {
        var food = foodRepository.findById(foodId);
        return food.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(null));
    }

    @GetMapping("/{foodId}/users")
    @Operation(summary = "주어진 ID를 가진 음식을 선호하는 모든 유저들 가져오기")
    public ResponseEntity<List<User>> getUserByFoodId(@PathVariable long foodId) {
        var food = foodRepository.findById(foodId);
        return food
                .map(value -> ResponseEntity.ok(value.getUsers()))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(null));
    }

    @GetMapping("/{foodId}/meal-info")
    @Operation(summary = "주어진 ID를 가진 음식을 선호하는 모든 유저들 가져오기")
    public ResponseEntity<List<MealInfo>> getMealInfoByFoodId(@PathVariable long foodId) {
        var food = foodRepository.findById(foodId);
        return food
                .map(value -> ResponseEntity.ok(value.getMealInfos()))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PutMapping("/{foodId}/meal-info/{mealInfoId}")
    @Operation(summary = "주어진 ID의 음식이 주어진 ID의 식단정보로 판별 정보 추가",
            description = "멱등성 - 여러번 추가해도 하나만 추가됨")
    @Transactional
    public ResponseEntity<Object> createFoodMealInfoRelation(@PathVariable long foodId, @PathVariable long mealInfoId) {
        var food = foodRepository.findById(foodId);
        var mealInfo = mealInfoRepository.findById(mealInfoId);

        if (food.isEmpty() || mealInfo.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } else {
            food.get().addMealInfo(mealInfo.get());
            return ResponseEntity.status(HttpStatus.valueOf(204)).build();
        }
    }

    @DeleteMapping("/{foodId}/meal-info/{mealInfoId}")
    @Operation(summary = "주어진 ID의 음식과 주어진 ID의 식단정보 판별 관계 삭제")
    public ResponseEntity<Object> deleteFoodMealInfoRelation(@PathVariable long foodId, @PathVariable long mealInfoId) {
        var food = foodRepository.findById(foodId);
        var mealInfo = mealInfoRepository.findById(mealInfoId);

        if (food.isEmpty() || mealInfo.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } else {
            food.get().removeMealInfo(mealInfo.get());
            return ResponseEntity.status(HttpStatus.valueOf(204)).build();
        }
    }
}
