package kr.ac.dankook.ace.healthy_meal_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class FoodResponseDTO {
    private List<String> foodResult;
    private List<Integer> foodWeight;
}
