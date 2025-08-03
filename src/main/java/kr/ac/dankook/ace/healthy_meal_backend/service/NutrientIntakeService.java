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
            // 이거 생각해보니까 'ml'도 있네.?ㅋㅋㅋㅋㅋㅋㅋ
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
