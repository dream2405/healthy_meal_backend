package kr.ac.dankook.ace.healthy_meal_backend.dto;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class NutrientValuesDTO {
    @Valid
    private List<NutrientValueElement> nutrientValues;
}
