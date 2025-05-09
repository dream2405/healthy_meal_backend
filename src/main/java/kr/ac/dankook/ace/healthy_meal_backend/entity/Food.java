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
public class Food {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // auto_increment 전략 사용
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "representative_food")
    private String representativeFood;

    @Column(name = "major_category")
    private String majorCategory;

    @Column(name = "medium_category")
    private String mediumCategory;

    @Column(name = "minor_category")
    private String minorCategory;

    @Column(name = "subcategory")
    private String subcategory;

    @Column(name = "nutri_ref_amt", length = 5)
    private String nutriRefAmt; // char(5) -> String 매핑

    @Column(name = "weight")
    private String weight;

    @Column(name = "energy_kcal")
    private Integer energyKcal; // nullable int -> Integer (래퍼 타입)

    @Column(name = "protein_g")
    private Double proteinG; // nullable double -> Double (래퍼 타입)

    @Column(name = "fat_g")
    private Double fatG;

    @Column(name = "carbohydrate_g")
    private Double carbohydrateG;

    @Column(name = "sugars_g")
    private Double sugarsG;

    @Column(name = "cellulose_g")
    private Double celluloseG;

    @Column(name = "sodium_mg")
    private Double sodiumMg;

    @Column(name = "cholesterol_mg")
    private Double cholesterolMg;

    // MealInfo와의 판별 다대다 연관관계
    @OneToMany(mappedBy = "food", cascade = CascadeType.ALL)
    private List<MealInfoFoodLink> mealInfoFoodLinks = new ArrayList<>();

    public void addMealInfo(MealInfo mealInfo) {
        // 이미 연결된 경우 중복 추가 방지
        for (MealInfoFoodLink link : mealInfoFoodLinks) {
            if (link.getMealInfo().getId().equals(mealInfo.getId())) {
                return; // 이미 연결되어 있으면 종료
            }
        }

        MealInfoFoodLink link = new MealInfoFoodLink();
        MealInfoFoodLinkId linkId = new MealInfoFoodLinkId();
        linkId.setFoodId(this.id);
        linkId.setMealInfoId(mealInfo.getId());

        link.setId(linkId);
        link.setFood(this);
        link.setMealInfo(mealInfo);

        this.mealInfoFoodLinks.add(link);
        mealInfo.getMealInfoFoodLinks().add(link);
    }

    public void removeMealInfo(MealInfo mealInfo) {
        Iterator<MealInfoFoodLink> iterator = this.mealInfoFoodLinks.iterator();

        while (iterator.hasNext()) {
            MealInfoFoodLink link = iterator.next();

            if (link.getMealInfo().equals(mealInfo)) {
                // 양쪽 컬렉션에서 제거
                iterator.remove(); // Food 쪽 컬렉션에서 제거
                mealInfo.getMealInfoFoodLinks().remove(link); // MealInfo 쪽 컬렉션에서 제거
            }
        }
    }

    public List<MealInfo> getMealInfos() {
        List<MealInfo> mealInfos = new ArrayList<>();
        for (MealInfoFoodLink link : this.mealInfoFoodLinks) {
            mealInfos.add(link.getMealInfo());
        }
        return mealInfos;
    }

    // 추가: User와의 다대다 관계를 위한 UserFoodLink 매핑
    @OneToMany(mappedBy = "food", fetch = FetchType.LAZY)
    private List<UserFoodLink> userFoodLinks = new ArrayList<>();

    // 추가: User 추가 메소드
    public void addUser(User user) {
        // 이미 연결된 경우 중복 추가 방지
        for (UserFoodLink link : userFoodLinks) {
            if (link.getUser().getId().equals(user.getId())) {
                return; // 이미 연결되어 있으면 종료
            }
        }

        UserFoodLink link = new UserFoodLink();
        UserFoodLinkId linkId = new UserFoodLinkId();
        linkId.setFoodId(this.id);
        linkId.setUserId(user.getId());

        link.setId(linkId);
        link.setFood(this);
        link.setUser(user);

        this.userFoodLinks.add(link);
        user.getUserFoodLinks().add(link);
    }

    // 추가: User 제거 메소드
    public void removeUser(User user) {
        Iterator<UserFoodLink> iterator = this.userFoodLinks.iterator();

        while (iterator.hasNext()) {
            UserFoodLink link = iterator.next();

            if (link.getUser().equals(user)) {
                iterator.remove(); // Food 쪽 컬렉션에서 제거
                user.getUserFoodLinks().remove(link); // User 쪽 컬렉션에서 제거
            }
        }
    }

    // 음식을 선호하는 모든 유저 반환
    public List<User> getUsers() {
        List<User> users = new ArrayList<>();
        for (UserFoodLink link : this.userFoodLinks) {
            users.add(link.getUser());
        }
        return users;
    }
}
