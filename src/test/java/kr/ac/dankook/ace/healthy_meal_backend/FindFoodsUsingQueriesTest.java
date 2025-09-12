package kr.ac.dankook.ace.healthy_meal_backend;

import kr.ac.dankook.ace.healthy_meal_backend.entity.Food;
import org.junit.jupiter.api.Test;

import java.util.Optional;

public class FindFoodsUsingQueriesTest extends HealthyMealBackendApplicationTests {
    @Test
    void testFindAll() {
        assert foodRepository.count() == 14751;
    }

    @Test
    void testFindFood() {
        Optional<Food> food = foodRepository.findById(1L);
        assert food.isPresent();
        assert food.get().getName().equals("국밥_돼지머리");
    }
}
