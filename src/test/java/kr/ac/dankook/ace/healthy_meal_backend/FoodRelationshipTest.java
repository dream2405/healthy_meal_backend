package kr.ac.dankook.ace.healthy_meal_backend;

import jakarta.transaction.Transactional;
import kr.ac.dankook.ace.healthy_meal_backend.entity.Food;
import kr.ac.dankook.ace.healthy_meal_backend.entity.MealInfo;
import kr.ac.dankook.ace.healthy_meal_backend.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class FoodRelationshipTest {
    @Autowired
    private TestEntityManager entityManager;

    @Test
    void testAddMealInfo() {
        // MealInfo에 필요한 User 생성
        User user = new User();
        user.setId("testUser");
        user.setGender('M');
        user.setHashedPassword("hashedPassword");
        entityManager.persist(user);

        // MealInfo 엔티티 생성 및 User와 연결
        MealInfo mealInfo = new MealInfo();
        mealInfo.setUser(user);
        entityManager.persist(mealInfo);

        // 편의 메소드를 사용하여 1번 Food에 MealInfo 추가
        Food food = entityManager.find(Food.class, 1L);
        food.addMealInfo(mealInfo);

        entityManager.flush();
        entityManager.clear();

        // 데이터베이스에서 Food와 MealInfo를 조회하여 관계 확인
        Food retrievedFood = entityManager.find(Food.class, food.getId());
        MealInfo retrievedMealInfo = entityManager.find(MealInfo.class, mealInfo.getId());

        // 관계가 올바르게 설정되었는지 확인
        assertNotNull(retrievedFood.getMealInfoFoodLinks());
        assertEquals(1, retrievedFood.getMealInfoFoodLinks().size());
        assertEquals(retrievedMealInfo.getId(), retrievedFood.getMealInfoFoodLinks().get(0).getMealInfo().getId());

        // 양방향 관계 확인
        List<Food> foods = retrievedMealInfo.getFoods();
        assertNotNull(foods);
        assertEquals(1, foods.size());
        assertEquals(retrievedFood.getId(), foods.get(0).getId());

        // 모든 관련 MealInfo를 가져오는 편의 메소드 테스트
        List<MealInfo> mealInfos = retrievedFood.getMealInfos();
        assertNotNull(mealInfos);
        assertEquals(1, mealInfos.size());
        assertEquals(retrievedMealInfo.getId(), mealInfos.get(0).getId());
    }
}
