package kr.ac.dankook.ace.healthy_meal_backend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class DailyIntakeElement {
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;
    private List<NutrientValueElement> nutrientValues;
}
