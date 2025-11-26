package kr.ac.dankook.ace.healthy_meal_backend.dto;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.lang.reflect.Field;
import java.util.List;

@Data
public class FoodElement {
    private String name;
    private Float intakeAmount;
    private Double energyKcal;
    private Double carbohydrateG;
    private Double proteinG;
    private Double calciumMg;
    private Double kaliumMg;
    private Double ironMg;
    private Double magnesiumMg;
    private Double zincMg;
    private Double celluloseG;
    private Double aminoacidMg;
    private Double leucineMg;
    private Double methionineMg;
    private Double seleniumUg;
    private Double omega3G;
    private Double vitaminAUg;
    private Double vitaminBMg;
    private Double folicacidUg;
    private Double vitaminB12Ug;
    private Double vitaminCMg;
    private Double vitaminDUg;
    private Double vitaminEMg;
}
