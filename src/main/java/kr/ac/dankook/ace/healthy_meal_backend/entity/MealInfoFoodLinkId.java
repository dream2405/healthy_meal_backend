package kr.ac.dankook.ace.healthy_meal_backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Hibernate;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@Embeddable
public class MealInfoFoodLinkId implements Serializable {
    @Serial
    private static final long serialVersionUID = 515181595630036943L;

    @Column(name = "meal_info_id", nullable = false)
    private Long mealInfoId;

    @Column(name = "food_id", nullable = false)
    private Long foodId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        MealInfoFoodLinkId entity = (MealInfoFoodLinkId) o;
        return Objects.equals(this.foodId, entity.foodId) &&
                Objects.equals(this.mealInfoId, entity.mealInfoId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(foodId, mealInfoId);
    }

}