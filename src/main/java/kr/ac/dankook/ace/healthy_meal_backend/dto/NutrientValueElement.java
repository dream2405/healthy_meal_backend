package kr.ac.dankook.ace.healthy_meal_backend.dto;

import kr.ac.dankook.ace.healthy_meal_backend.validation.EnumValid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class NutrientValueElement {
    @EnumValid(enumClass = NutrientType.class)
    private String nutrientName;
    private Double value;
}
