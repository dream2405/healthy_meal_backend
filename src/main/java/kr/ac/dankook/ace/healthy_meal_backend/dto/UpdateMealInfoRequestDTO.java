package kr.ac.dankook.ace.healthy_meal_backend.dto;

import lombok.Data;

import java.util.List;

@Data
public class UpdateMealInfoRequestDTO {
    private String diary;
    private List<Float> intakeAmounts;
    private List<String> confirmedFoods;
}
