package kr.ac.dankook.ace.healthy_meal_backend.entity;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FoodCluster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cluster_num")
    private Long clusterNum;

    @Column(name = "description")
    private String description;

    @Column(name = "high_nutrients")
    private String highNutrients;

    @Column(name = "low_nutrients")
    private String lowNutrients;

    @Column(name = "nutri_vector")
    private String nutriVector;
}
