package kr.ac.dankook.ace.healthy_meal_backend;

import jakarta.annotation.PostConstruct;
import kr.ac.dankook.ace.healthy_meal_backend.repository.FoodRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.TimeZone;

@SpringBootApplication
@EnableJpaAuditing
public class HealthyMealBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(HealthyMealBackendApplication.class, args);
    }

    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
    }

    @Bean
    public ApplicationRunner configure(FoodRepository foodRepository) {
        return env -> {
            System.out.println(foodRepository.findById(1L));
        };
    }
}
