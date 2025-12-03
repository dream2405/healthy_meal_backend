package kr.ac.dankook.ace.healthy_meal_backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Setter
@Entity
@Table(name = "mealrecord_food_link")
public class MealRecordFoodLink {
    @EmbeddedId
    private MealRecordFoodLinkId id = new MealRecordFoodLinkId();

    @MapsId("mealRecordId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "mealrecord_id", nullable = false)
    private MealRecord mealRecord;

    @MapsId("foodId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "food_id", nullable = false)
    private Food food;

    @Column(name = "intake_amount")
    private Float intakeAmount;
}