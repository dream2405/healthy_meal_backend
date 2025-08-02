package kr.ac.dankook.ace.healthy_meal_backend.service;

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
import java.util.Optional;

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

    public DailyIntake updateDailyIntake(MealInfo mealInfo, User user, LocalDate date) {
        DailyIntake dailyIntake = dailyIntakeRepository.findByUserIdAndDay(user.getId(), date)
                .stream()
                .findFirst()
                .orElse(createNewDailyIntake(user, date));

        List<MealInfo> mealInfos = mealInfoRepository.findByUserIdAndCreatedDate(user.getId(), date);
        if (mealInfos.isEmpty()) { throw new NoSuchElementException("기록된 식단정보 없음");}

        mealInfos.stream()
                .flatMap(varMealInfo -> mealInfo.getFoods().stream())
                .forEach(food -> addFoodNutrition(dailyIntake, food));

        return dailyIntakeRepository.save(dailyIntake);
    }

    private DailyIntake createNewDailyIntake(User user, LocalDate date) {
        DailyIntake dailyIntake = new DailyIntake();
        dailyIntake.setUser(user);
        dailyIntake.setDay(date);
        return dailyIntake;
    }
    private void addFoodNutrition(DailyIntake dailyIntake, Food food) {
        dailyIntake.setCelluloseG(dailyIntake.getCelluloseG() +
                Optional.ofNullable(food.getCelluloseG()).orElse(0.0).floatValue());
        dailyIntake.setFatG(dailyIntake.getFatG() +
                Optional.ofNullable(food.getFatG()).orElse(0.0).floatValue());
        dailyIntake.setCarbohydrateG(dailyIntake.getCarbohydrateG() +
                Optional.ofNullable(food.getCarbohydrateG()).orElse(0.0).floatValue());
        dailyIntake.setProteinG(dailyIntake.getProteinG() +
                Optional.ofNullable(food.getProteinG()).orElse(0.0).floatValue());
        dailyIntake.setCholesterolMg(dailyIntake.getCholesterolMg() +
                Optional.ofNullable(food.getCholesterolMg()).orElse(0.0).floatValue());
        dailyIntake.setEnergyKcal(dailyIntake.getEnergyKcal() +
                Optional.ofNullable(food.getEnergyKcal()).orElse(0));
        dailyIntake.setSugarsG(dailyIntake.getSugarsG() +
                Optional.ofNullable(food.getSugarsG()).orElse(0.0).floatValue());
        dailyIntake.setSodiumMg(dailyIntake.getSodiumMg() +
                Optional.ofNullable(food.getSodiumMg()).orElse(0.0).floatValue());
    }
}
