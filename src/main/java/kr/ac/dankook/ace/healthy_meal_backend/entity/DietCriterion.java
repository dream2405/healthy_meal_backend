package kr.ac.dankook.ace.healthy_meal_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
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
    private Double energyKcal;

    @Column(name = "carbohydrate_g")
    private Double carbohydrateG;

    @Column(name = "protein_g")
    private Double proteinG;

    @Column(name = "calcium_mg")
    private Double calciumMg;

    @Column(name = "kalium_mg")
    private Double kaliumMg;

    @Column(name = "iron_mg")
    private Double ironMg;

    @Column(name = "magnesium_mg")
    private Double magnesiumMg;

    @Column(name = "zinc_mg")
    private Double zincMg;

    @Column(name = "cellulose_g")
    private Double celluloseG;

    @Column(name = "aminoacid_mg")
    private Double aminoacidMg;

    @Column(name = "leucine_mg")
    private Double leucineMg;

    @Column(name = "methionine_mg")
    private Double methionineMg;

    @Column(name = "selenium_ug")
    private Double seleniumUg;

    @Column(name = "omega3_g")
    private Double omega3G;

    @Column(name = "vitaminA_ug")
    private Double vitaminAUg;

    @Column(name = "vitaminB_mg")
    private Double vitaminBMg;

    @Column(name = "folicacid_ug")
    private Double folicacidUg;

    @Column(name = "vitaminB12_ug")
    private Double vitaminB12Ug;

    @Column(name = "vitaminC_mg")
    private Double vitaminCMg;

    @Column(name = "vitaminD_ug")
    private Double vitaminDUg;

    @Column(name = "vitaminE_mg")
    private Double vitaminEMg;
}