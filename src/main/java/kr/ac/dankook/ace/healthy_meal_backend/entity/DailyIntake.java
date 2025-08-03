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

    @Column(name = "energy_kcal")
    private Double energyKcal = 0d;

    @Column(name = "protein_g")
    private Double proteinG = 0d;

    @Column(name = "fat_g")
    private Double fatG = 0d;

    @Column(name = "carbohydrate_g")
    private Double carbohydrateG = 0d;

    @Column(name = "sugars_g")
    private Double sugarsG = 0d;

    @Column(name = "cellulose_g")
    private Double celluloseG = 0d;

    @Column(name = "sodium_mg")
    private Double sodiumMg = 0d;

    @Column(name = "cholesterol_mg")
    private Double cholesterolMg = 0d;

    @Column(name = "score")
    private Integer score;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // 일별 섭취 기록을 기록한 유저

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
}