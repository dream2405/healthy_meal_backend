package kr.ac.dankook.ace.healthy_meal_backend.dto;

import lombok.Data;

import java.time.Instant;

@Data
public class MealInfoPostDTO {
    private Long id;
    private Integer intakeAmount;
    private String imgPath;
    private String diary;
    private Instant createdAt;
    private Instant lastModifiedAt;
}
