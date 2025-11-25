package kr.ac.dankook.ace.healthy_meal_backend.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class MealRecordRequestDTO {
    private Long id;
    private String mealName;
    private String imgPath;
    private LocalDateTime takenAt;
    private List<String> confirmedFoods;
    private List<Double> intakeAmounts;
    private String diary;
}
