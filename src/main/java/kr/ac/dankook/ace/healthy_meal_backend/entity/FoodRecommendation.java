package kr.ac.dankook.ace.healthy_meal_backend.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // Lombok: JPA를 위한 기본 생성자 자동 생성 (protected 접근 수준)
@ToString(exclude = {"mealInfoFoodLinks", "userFoodLinks"})
public class FoodRecommendation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // auto_increment 전략 사용
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "category")
    private String category;

    @Column(name = "energy_kcal")
    private Double energyKcal; // nullable int -> Integer (래퍼 타입)

    @Column(name = "protein_g")
    private Double proteinG; // nullable double -> Double (래퍼 타입)

    @Column(name = "fat_g")
    private Double fatG;

    @Column(name = "carbohydrate_g")
    private Double carbohydrateG;

    @Column(name = "sugars_g")
    private Double sugarsG;

    @Column(name = "sodium_mg")
    private Double sodiumMg;

    @Column(name = "cholesterol_mg")
    private Double cholesterolMg;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cluster_num")
    private FoodCluster foodCluster;
}
