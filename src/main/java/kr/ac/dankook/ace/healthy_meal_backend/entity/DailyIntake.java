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

    @Column(name = "score")
    private Integer score;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // 일별 섭취 기록을 기록한 유저
}