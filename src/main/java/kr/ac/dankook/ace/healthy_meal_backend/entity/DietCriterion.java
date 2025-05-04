package kr.ac.dankook.ace.healthy_meal_backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "diet_criterion")
public class DietCriterion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "start_age")
    private Integer startAge;

    @Column(name = "end_age")
    private Integer endAge;

    @Column(name = "gender")
    private Character gender;

    @Column(name = "energy_kcal")
    private Integer energyKcal;

    @Column(name = "protein_g")
    private Float proteinG;

    @Column(name = "fat_g")
    private Float fatG;

    @Column(name = "carbohydrate_g")
    private Float carbohydrateG;

    @Column(name = "sugars_g")
    private Float sugarsG;

    @Column(name = "cellulose_g")
    private Float celluloseG;

    @Column(name = "sodium_mg")
    private Float sodiumMg;

    @Column(name = "cholesterol_mg")
    private Float cholesterolMg;

}