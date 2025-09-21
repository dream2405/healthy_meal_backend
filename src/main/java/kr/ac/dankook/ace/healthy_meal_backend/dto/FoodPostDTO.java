package kr.ac.dankook.ace.healthy_meal_backend.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class FoodPostDTO {
    @NotBlank
    private String name;
    @NotBlank
    private String representativeFood;
    @NotBlank
    private String majorCategory;
    @NotBlank
    private String mediumCategory;
    @NotBlank
    private String minorCategory;
    @NotBlank
    private String subcategory;
    @NotBlank
    @Size(min = 5, max = 5)
    private String nutriRefAmt; // char(5) -> String 매핑
    @NotBlank
    private String weight;
    @NotNull
    private Integer energyKcal; // nullable int -> Integer (래퍼 타입)
    @NotNull
    private Double proteinG; // nullable double -> Double (래퍼 타입)
    @NotNull
    private Double fatG;
    @NotNull
    private Double carbohydrateG;
    @NotNull
    private Double sugarsG;
    @NotNull
    private Double celluloseG;
    @NotNull
    private Double sodiumMg;
    @NotNull
    private Double cholesterolMg;
}
