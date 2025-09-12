package kr.ac.dankook.ace.healthy_meal_backend;

import kr.ac.dankook.ace.healthy_meal_backend.repository.FoodRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class HealthyMealBackendApplicationTests {

    @Autowired
    FoodRepository foodRepository;

    @Test
    void contextLoads() {
    }

}
