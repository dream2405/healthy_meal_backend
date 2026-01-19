package kr.ac.dankook.ace.healthy_meal_backend.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class NutrientModelDTO {
    private String modelname;
    private Float nutrientLevel;
}
