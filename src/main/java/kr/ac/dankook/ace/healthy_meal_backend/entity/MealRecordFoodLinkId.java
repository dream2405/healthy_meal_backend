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
public class MealRecordFoodLinkId implements Serializable {
    @Serial
    private static final long serialVersionUID = 515181595630036943L;

    @Column(name = "mealrecord_id", nullable = false)
    private Long mealRecordId;

    @Column(name = "food_id", nullable = false)
    private Long foodId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        MealRecordFoodLinkId entity = (MealRecordFoodLinkId) o;
        return Objects.equals(this.foodId, entity.foodId) &&
                Objects.equals(this.mealRecordId, entity.mealRecordId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(foodId, mealRecordId);
    }

}