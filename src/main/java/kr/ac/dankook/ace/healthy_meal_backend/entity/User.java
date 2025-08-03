package kr.ac.dankook.ace.healthy_meal_backend.entity;

import jakarta.persistence.*;
import kr.ac.dankook.ace.healthy_meal_backend.dto.DietCriterionWeightDTO;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "user")
public class User {
    @Id
    @Column(name = "id", nullable = false)
    private String id;

    @Column(name = "hashed_password", nullable = false)
    private String hashedPassword;

    @Column(name = "birthday")
    private LocalDate birthday;

    @Column(name = "gender")
    private Character gender;

    @Column(name = "critweight")
    private String critweight;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<NutriWeight> nutriWeights = new ArrayList<>(); // 유저가 설정한 가중치들

    public void addNutriWeight(NutriWeight nutriWeight) {
        this.nutriWeights.add(nutriWeight);
        nutriWeight.setUser(this);
    }

    public void removeNutriWeight(NutriWeight nutriWeight) {
        this.nutriWeights.remove(nutriWeight);
    }

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<DailyIntake> dailyIntakes = new ArrayList<>(); // 유저가 기록한 일별 섭취 기록

    public void addDailyIntake(DailyIntake dailyIntake) {
        this.dailyIntakes.add(dailyIntake);
        dailyIntake.setUser(this);
    }

    public void removeDailyIntake(DailyIntake dailyIntake) {
        this.dailyIntakes.remove(dailyIntake);
    }

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<MealInfo> mealInfos = new ArrayList<>();

    /*
        1:N 관계에서의 정합성 처리
        1의 테이블 입장에서는 N 엔티티 정보를 가지고 있지 않음 but,
        1의 자바객체 입장에서는 N 엔티티 정보를 조회하는 함수를 만들 수 있음
        -> 따라서 삽입 / 삭제시 @OneToMany 필드에 대한 작업도 필요함
        -> 실제로 테이블에는 @OneToMany 속성은 존재하지않음
     */
    public void addMealInfo(MealInfo mealInfo) {
        this.mealInfos.add(mealInfo);       // for 자바내부객체 컬렉션 정합성 (실제론 userentity에 mealinfo정보는 없음)
        mealInfo.setUser(this);             // 관계성의 주인은 mealinfo -> user entity 참조를 추가함
    }

    public void removeMealInfo(MealInfo mealInfo) {
        this.mealInfos.remove(mealInfo);
        mealInfo.setUser(null);
    }

    // Food와의 다대다 관계를 위한 UserFoodLink 매핑
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<UserFoodLink> userFoodLinks = new ArrayList<>();

    public void addFood(Food food) {
        // 이미 연결된 경우 중복 추가 방지
        for (UserFoodLink link : userFoodLinks) {
            if (link.getFood().getId().intValue() == food.getId().intValue()) {
                return; // 이미 연결되어 있으면 종료
            }
        }

        UserFoodLink link = new UserFoodLink();
        UserFoodLinkId linkId = new UserFoodLinkId();
        linkId.setUserId(this.id);
        linkId.setFoodId(food.getId());

        link.setId(linkId);
        link.setUser(this);
        link.setFood(food);

        this.userFoodLinks.add(link);
        food.getUserFoodLinks().add(link);
    }

    // Food 제거 메소드
    public void removeFood(Food food) {
        Iterator<UserFoodLink> iterator = this.userFoodLinks.iterator();

        while (iterator.hasNext()) {
            UserFoodLink link = iterator.next();

            if (link.getFood().equals(food)) {
                iterator.remove();  // User 쪽 컬렉션에서 제거
                food.getUserFoodLinks().remove(link);  // Food 쪽 컬렉션에서도 제거
            }
        }
    }

    // 유저가 선호하는 모든 음식 반환
    public List<Food> getFoods() {
        List<Food> foods = new ArrayList<>();
        for (UserFoodLink link : this.userFoodLinks) {
            foods.add(link.getFood());
        }
        return foods;
    }

    public void updateCritweight(String critwegith) {
        this.critweight = critwegith;
    }
}