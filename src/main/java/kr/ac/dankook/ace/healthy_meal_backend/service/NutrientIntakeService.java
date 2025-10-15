package kr.ac.dankook.ace.healthy_meal_backend.service;

import jakarta.transaction.Transactional;
import kr.ac.dankook.ace.healthy_meal_backend.entity.*;
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
    public void applyInsertDailyIntake(MealInfo mealInfo, User user) {
        LocalDate now = LocalDate.now();
        DailyIntake dailyIntake = dailyIntakeRepository.findByUserIdAndDay(user.getId(), now)
                .stream()
                .findFirst()
                .orElseGet(() -> dailyIntakeRepository.save(createNewDailyIntake(user, now)));
        int foodNum = mealInfo.getFoods().size();
        mealInfo.getFoodLink().forEach(foodLink -> addFoodNutrition(dailyIntake, foodLink));
    }
    private DailyIntake createNewDailyIntake(User user, LocalDate now) {
        DailyIntake dailyIntake = new DailyIntake();
        dailyIntake.setUser(user);
        dailyIntake.setDay(now);
        return dailyIntake;
    }
    private void addFoodNutrition(DailyIntake dailyIntake, MealInfoFoodLink foodLink) {
        try {
            Food food = foodLink.getFood();
            float intakeRatio = Float.parseFloat(food.getWeight().replaceAll("[^\\d.]", "")) / 100;
            intakeRatio *= foodLink.getIntakeAmount();
            System.out.println("계산된 음식중량 비율 : " + intakeRatio);
            System.out.println("이전 칼로리 섭취량 : " + dailyIntake.getEnergyKcal());
            dailyIntake.addMealIntake(
                    nullToZero(food.getEnergyKcal())*intakeRatio,
                    nullToZero(food.getProteinG())*intakeRatio,
                    nullToZero(food.getFatG())*intakeRatio,
                    nullToZero(food.getCarbohydrateG())*intakeRatio,
                    nullToZero(food.getSugarsG())*intakeRatio,
                    nullToZero(food.getCelluloseG())*intakeRatio,
                    nullToZero(food.getSodiumMg())*intakeRatio,
                    nullToZero(food.getCholesterolMg())*intakeRatio
            );
            System.out.println("기록된 칼로리량 : " + nullToZero(food.getEnergyKcal())*intakeRatio);
            System.out.println("기록된 단백질량 : " + nullToZero(food.getProteinG())*intakeRatio);
            System.out.println("기록된 지방량 : " + nullToZero(food.getFatG())*intakeRatio);
            System.out.println("기록된 탄수화물량 : " + nullToZero(food.getCarbohydrateG())*intakeRatio);
            System.out.println("기록된 당류량 : " + nullToZero(food.getSugarsG())*intakeRatio);
            System.out.println("기록된 식이섬유량 : " + nullToZero(food.getCelluloseG())*intakeRatio);
            System.out.println("기록된 나트륨량 : " + nullToZero(food.getSodiumMg())*intakeRatio);
            System.out.println("기록된 콜레스테롤량 : " + nullToZero(food.getCholesterolMg())*intakeRatio);
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
