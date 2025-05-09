package kr.ac.dankook.ace.healthy_meal_backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

@Getter
@Setter
@Entity
@Table(name = "meal_info")
public class MealInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "intake_amount")
    private Integer intakeAmount;

    @Column(name = "img_path")
    private String imgPath;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at")
    private Instant createdAt;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "last_modified_at")
    private Instant lastModifiedAt;

    @Lob
    @Column(name = "diary")
    private String diary;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // 식단 정보를 기록한 유저

    // Food와의 판별 다대다 연관관계
    @OneToMany(mappedBy = "mealInfo", cascade = CascadeType.ALL)
    private List<MealInfoFoodLink> mealInfoFoodLinks = new ArrayList<>();

    // 연관관계 편의 메서드
    public void addFood(Food food) {
        // 이미 연결된 경우 중복 추가 방지
        for (MealInfoFoodLink link : mealInfoFoodLinks) {
            if (link.getFood().getId().equals(food.getId())) {
                return; // 이미 연결되어 있으면 종료
            }
        }

        MealInfoFoodLink link = new MealInfoFoodLink();
        MealInfoFoodLinkId linkId = new MealInfoFoodLinkId();
        linkId.setMealInfoId(this.id);
        linkId.setFoodId(food.getId());

        link.setId(linkId);
        link.setMealInfo(this);
        link.setFood(food);

        this.mealInfoFoodLinks.add(link);
        food.getMealInfoFoodLinks().add(link);
    }

    public void removeFood(Food food) {
        Iterator<MealInfoFoodLink> iterator = this.mealInfoFoodLinks.iterator();

        while (iterator.hasNext()) {
            MealInfoFoodLink link = iterator.next();

            if (link.getFood().equals(food)) {
                // 양쪽 컬렉션에서 제거
                iterator.remove(); // MealInfo 쪽 컬렉션에서 제거
                food.getMealInfoFoodLinks().remove(link); // Food 쪽 컬렉션에서 제거
            }
        }
    }

    // 편의 메서드: 연관된 모든 Food 객체를 가져오는 메서드
    public List<Food> getFoods() {
        List<Food> foods = new ArrayList<>();
        for (MealInfoFoodLink link : this.mealInfoFoodLinks) {
            foods.add(link.getFood());
        }
        return foods;
    }
}