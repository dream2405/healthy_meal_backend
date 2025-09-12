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
public class UserFoodLinkId implements Serializable {
    @Serial
    private static final long serialVersionUID = -6349389821461068582L;

    @Column(name = "food_id", nullable = false)
    private Long foodId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        UserFoodLinkId entity = (UserFoodLinkId) o;
        return Objects.equals(this.foodId, entity.foodId) &&
                Objects.equals(this.userId, entity.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(foodId, userId);
    }

}