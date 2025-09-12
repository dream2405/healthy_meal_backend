package kr.ac.dankook.ace.healthy_meal_backend.dto;

import lombok.Data;

@Data
public class FoodDTO {
    private Long id;
    private String name;
    private String representativeFood;
    private String majorCategory;
    private String mediumCategory;
    private String minorCategory;
    private String subcategory;
    private String nutriRefAmt; // char(5) -> String 매핑
    private String weight;
    private Integer energyKcal; // nullable int -> Integer (래퍼 타입)
    private Double proteinG; // nullable double -> Double (래퍼 타입)
    private Double fatG;
    private Double carbohydrateG;
    private Double sugarsG;
    private Double celluloseG;
    private Double sodiumMg;
    private Double cholesterolMg;
}
