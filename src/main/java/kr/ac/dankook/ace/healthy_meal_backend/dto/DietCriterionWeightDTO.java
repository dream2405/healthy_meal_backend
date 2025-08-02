package kr.ac.dankook.ace.healthy_meal_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class DietCriterionWeightDTO {
    private Integer energyKcalWeight;
    private Integer carbohydrateGWeight;
    private Integer fatGWeight;
    private Integer proteinGWeight;
    private Integer celluloseGWeight;
    private Integer sugarsGWeight;
    private Integer sodiumMgWeight;
    private Integer cholesterolMgWeight;

    public List<Integer> toList() {
        return List.of(energyKcalWeight, carbohydrateGWeight, fatGWeight, proteinGWeight, celluloseGWeight, sugarsGWeight, sodiumMgWeight,  cholesterolMgWeight);
    }
}
