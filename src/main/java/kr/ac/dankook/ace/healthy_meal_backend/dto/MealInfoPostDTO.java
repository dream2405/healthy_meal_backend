package kr.ac.dankook.ace.healthy_meal_backend.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class MealInfoPostDTO {
    private Long id;
    private Float intakeAmount;
    private String imgPath;
    private String diary;
    private LocalDateTime createdAt;
    private LocalDateTime lastModifiedAt;
    private List<MealInfoFoodLinkDTO> mealInfoFoodLinks;
}
