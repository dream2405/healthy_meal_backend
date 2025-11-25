package kr.ac.dankook.ace.healthy_meal_backend.dto;

import lombok.Data;

import java.util.List;

@Data
public class NutrientValuesDTO {
    private List<NutrientValueElement> nutrientValues;
}
