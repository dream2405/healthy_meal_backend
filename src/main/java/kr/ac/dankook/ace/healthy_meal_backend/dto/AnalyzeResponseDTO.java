package kr.ac.dankook.ace.healthy_meal_backend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@Data
public class AnalyzeResponseDTO {
    private List<String> foodResult;
    private List<Integer> foodWeight;
}
