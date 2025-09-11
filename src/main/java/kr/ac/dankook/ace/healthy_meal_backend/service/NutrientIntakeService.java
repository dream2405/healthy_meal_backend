package kr.ac.dankook.ace.healthy_meal_backend.service;

import jakarta.transaction.Transactional;
import kr.ac.dankook.ace.healthy_meal_backend.entity.DailyIntake;
import kr.ac.dankook.ace.healthy_meal_backend.entity.Food;
import kr.ac.dankook.ace.healthy_meal_backend.entity.MealInfo;
import kr.ac.dankook.ace.healthy_meal_backend.entity.User;
import kr.ac.dankook.ace.healthy_meal_backend.repository.DailyIntakeRepository;
import kr.ac.dankook.ace.healthy_meal_backend.repository.MealInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class NutrientIntakeService {

    private final DailyIntakeRepository dailyIntakeRepository;
    private final MealInfoRepository mealInfoRepository;

    @Autowired
    public NutrientIntakeService (
        DailyIntakeRepository dailyIntakeRepository,
        MealInfoRepository mealInfoRepository
    ) {
        this.dailyIntakeRepository = dailyIntakeRepository;
        this.mealInfoRepository = mealInfoRepository;
    }

    public List<DailyIntake> getDailyIntakes(String userId) {
        return dailyIntakeRepository.findByUserId(userId);
    }

    @Transactional
    public void applyInsertDailyIntake(MealInfo mealInfo, User user, LocalDate date) {
        DailyIntake dailyIntake = dailyIntakeRepository.findByUserIdAndDay(user.getId(), date)
                .stream()
                .findFirst()
                .orElseGet(() -> dailyIntakeRepository.save(createNewDailyIntake(user, date)));
        int foodNum = mealInfo.getFoods().size();
        mealInfo.getFoods().forEach(food -> addFoodNutrition(dailyIntake, food, foodNum));
    }
    private DailyIntake createNewDailyIntake(User user, LocalDate date) {
        DailyIntake dailyIntake = new DailyIntake();
        dailyIntake.setUser(user);
        dailyIntake.setDay(date);
        return dailyIntake;
    }
    private void addFoodNutrition(DailyIntake dailyIntake, Food food, int foodNum) {
        try {
            float calRatio = Float.parseFloat(food.getWeight().replaceAll("[^\\d.]", "")) / 100;
            System.out.println("계산된 음식중량 비율 : " + calRatio + " & 음식개수 : " + foodNum);
            System.out.println("이전 칼로리 섭취량 : " + dailyIntake.getEnergyKcal());
            dailyIntake.addMealIntake(
                    nullToZero(food.getEnergyKcal())*calRatio/foodNum,
                    nullToZero(food.getProteinG())*calRatio/foodNum,
                    nullToZero(food.getFatG())*calRatio/foodNum,
                    nullToZero(food.getCarbohydrateG())*calRatio/foodNum,
                    nullToZero(food.getSugarsG())*calRatio/foodNum,
                    nullToZero(food.getCelluloseG())*calRatio/foodNum,
                    nullToZero(food.getSodiumMg())*calRatio/foodNum,
                    nullToZero(food.getCholesterolMg())*calRatio/foodNum
            );
            System.out.println("기록된 칼로리량 : " + nullToZero(food.getEnergyKcal())*calRatio/foodNum);
            System.out.println("기록된 단백질량 : " + nullToZero(food.getProteinG())*calRatio/foodNum);
            System.out.println("기록된 지방량 : " + nullToZero(food.getFatG())*calRatio/foodNum);
            System.out.println("기록된 탄수화물량 : " + nullToZero(food.getCarbohydrateG())*calRatio/foodNum);
            System.out.println("기록된 당류량 : " + nullToZero(food.getSugarsG())*calRatio/foodNum);
            System.out.println("기록된 식이섬유량 : " + nullToZero(food.getCelluloseG())*calRatio/foodNum);
            System.out.println("기록된 나트륨량 : " + nullToZero(food.getSodiumMg())*calRatio/foodNum);
            System.out.println("기록된 콜레스테롤량 : " + nullToZero(food.getCholesterolMg())*calRatio/foodNum);
            System.out.println("이후 칼로리 섭취량 : " + dailyIntake.getEnergyKcal());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
    private Double nullToZero(Double value) {
        return value != null ? value : 0d;
    }

    @Transactional
    public void applyDeleteDailyIntake(MealInfo mealInfo, User user, LocalDate date) {
        DailyIntake dailyIntake = dailyIntakeRepository.findByUserIdAndDay(user.getId(), date)
                .stream()
                .findFirst()
                .orElseThrow(NoSuchElementException::new);
        int foodNum = mealInfo.getFoods().size();
        mealInfo.getFoods().forEach(food -> deleteFoodNutrition(dailyIntake, food, foodNum));
    }
    private void deleteFoodNutrition(DailyIntake dailyIntake, Food food,  int foodNum) {
        try {
            float calRatio = Float.parseFloat(food.getWeight().replaceAll("[^\\d.]", "")) / 100;
            dailyIntake.deleteMealIntake(
                    nullToZero(food.getEnergyKcal())*calRatio/foodNum,
                    nullToZero(food.getProteinG())*calRatio/foodNum,
                    nullToZero(food.getFatG())*calRatio/foodNum,
                    nullToZero(food.getCarbohydrateG())*calRatio/foodNum,
                    nullToZero(food.getSugarsG())*calRatio/foodNum,
                    nullToZero(food.getCelluloseG())*calRatio/foodNum,
                    nullToZero(food.getSodiumMg())*calRatio/foodNum,
                    nullToZero(food.getCholesterolMg())*calRatio/foodNum
            );
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
