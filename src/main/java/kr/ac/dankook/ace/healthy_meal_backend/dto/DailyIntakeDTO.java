package kr.ac.dankook.ace.healthy_meal_backend.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class DailyIntakeDTO {
    private Integer id;
    private LocalDate day;
    private Integer energyKcal;
    private Float proteinG;
    private Float fatG;
    private Float carbohydrateG;
    private Float sugarsG;
    private Float celluloseG;
    private Float sodiumMg;
    private Float cholesterolMg;
    private Integer score;
}
