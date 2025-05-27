package kr.ac.dankook.ace.healthy_meal_backend.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MealInfoPostDTO {
    private Long id;
    private Integer intakeAmount;
    private String imgPath;
    private String diary;
    private LocalDateTime createdAt;
    private LocalDateTime lastModifiedAt;
}
