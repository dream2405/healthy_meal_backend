package kr.ac.dankook.ace.healthy_meal_backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.ac.dankook.ace.healthy_meal_backend.entity.Food;
import kr.ac.dankook.ace.healthy_meal_backend.repository.FoodRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/foods")
@Tag(name = "음식", description = "Test Desc")
public class FoodController {
    private final FoodRepository foodRepository;

    @Autowired
    public FoodController(final FoodRepository foodRepository) {
        this.foodRepository = foodRepository;
    }

    @GetMapping("/{name}")
    @Operation(summary = "주어진 음식 이름으로 모든 음식들 가져오기")
    public List<Food> getFood(@PathVariable String name) {
        return foodRepository.findAllByName(name);
    }

    @GetMapping("/{foodId}")
    @Operation(summary = "주어진 ID를 가진 특정 음식 가져오기")
    public Optional<Food> getFoodById(@PathVariable int foodId) {
        return foodRepository.findById((long) foodId);
    }
}
