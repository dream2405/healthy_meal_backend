package kr.ac.dankook.ace.healthy_meal_backend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class MealRecordDTO {
    private Long id;
    private String imgPath;
    private String mealName;
    @JsonFormat(pattern = "yyyy-MM-dd-hh-mm")
    private LocalDateTime takenAt;
    private String diary;
    private List<FoodElement> foods;
}
