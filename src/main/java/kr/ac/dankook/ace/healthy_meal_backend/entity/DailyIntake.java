package kr.ac.dankook.ace.healthy_meal_backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "daily_intake", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "day"}))
public class DailyIntake {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "day")
    private LocalDate day;

    @Column(name = "dailyscore")
    private Integer dailyscore;

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

    @Column(name = "vitaminB12_mg")
    private Double vitaminB12Mg;

    @Column(name = "vitaminC_mg")
    private Double vitaminCMg;

    @Column(name = "vitaminD_ug")
    private Double vitaminDUg;

    @Column(name = "vitaminE_mg")
    private Double vitaminEMg;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // 일별 섭취 기록을 기록한 유저

    /*
    public void addMealIntake(double cal, double pro, double fat, double car, double sug, double cel, double sod, double cho) {
        this.energyKcal += cal;
        this.proteinG += pro;
        this.fatG += fat;
        this.carbohydrateG += car;
        this.sugarsG += sug;
        this.celluloseG += cel;
        this.sodiumMg += sod;
        this.cholesterolMg += cho;
    }

    public void deleteMealIntake(double cal, double pro, double fat, double car, double sug, double cel, double sod, double cho) {
        if (this.energyKcal > 0) {
            this.energyKcal -= cal;
        }
        if (this.proteinG > 0) {
            this.proteinG -= pro;
        }
        if (this.fatG > 0) {
            this.fatG -= fat;
        }
        if (this.carbohydrateG > 0) {
            this.carbohydrateG -= car;
        }
        if (this.sugarsG > 0) {
            this.sugarsG -= sug;
        }
        if (this.celluloseG > 0) {
            this.celluloseG -= cel;
        }
        if (this.sodiumMg > 0) {
            this.sodiumMg -= sod;
        }
        if (this.cholesterolMg > 0) {
            this.cholesterolMg -= cho;
        }
    }

    */
}
