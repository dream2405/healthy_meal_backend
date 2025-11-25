package kr.ac.dankook.ace.healthy_meal_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@AllArgsConstructor
@Data
public class DailyIntakeDTO {
    private List<DailyIntakeElement> dailyintakes;
}
