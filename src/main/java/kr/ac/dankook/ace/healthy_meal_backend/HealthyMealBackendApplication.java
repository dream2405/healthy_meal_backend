package kr.ac.dankook.ace.healthy_meal_backend;

import kr.ac.dankook.ace.healthy_meal_backend.repository.FoodRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class HealthyMealBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(HealthyMealBackendApplication.class, args);
    }

    @Bean
    public ApplicationRunner configure(FoodRepository foodRepository) {
        return env -> {
            System.out.println(foodRepository.findById(1L));
        };
    }
}
