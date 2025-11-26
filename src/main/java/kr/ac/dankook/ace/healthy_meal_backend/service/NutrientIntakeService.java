package kr.ac.dankook.ace.healthy_meal_backend.service;

import jakarta.transaction.Transactional;
import kr.ac.dankook.ace.healthy_meal_backend.dto.DailyIntakeElement;
import kr.ac.dankook.ace.healthy_meal_backend.dto.NutrientValueElement;
import kr.ac.dankook.ace.healthy_meal_backend.dto.NutrientValuesDTO;
import kr.ac.dankook.ace.healthy_meal_backend.entity.*;
import kr.ac.dankook.ace.healthy_meal_backend.repository.DailyIntakeRepository;
import kr.ac.dankook.ace.healthy_meal_backend.repository.NutrientWeightRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class NutrientIntakeService {

    private final DailyIntakeRepository dailyIntakeRepository;
    private final NutrientWeightRepository nutrientWeightRepository;

    public List<NutrientValueElement> getDailyIntake(String userId, LocalDate date) {
        List<NutrientValueElement> nutrientValueElements = new ArrayList<>();
        List<NutrientWeight> nutrientWeights = nutrientWeightRepository.findByUserId(userId);
        DailyIntake dailyIntake = dailyIntakeRepository.findByUserIdAndDay(userId, date)
                .orElseThrow(NoSuchElementException::new);
        for (NutrientWeight nutrientWeight : nutrientWeights) {
            NutrientValueElement nutrientValueElement = new NutrientValueElement(nutrientWeight.getNutrientName(), dailyIntake.getValue(nutrientWeight.getNutrientName()));
            nutrientValueElements.add(nutrientValueElement);
        }
        return nutrientValueElements;
    }

    public List<DailyIntakeElement> getDailyIntakes(String userId, LocalDate startDate, LocalDate endDate) {
        List<DailyIntakeElement> dailyIntakeElements = new ArrayList<>();
        List<DailyIntake> dailyIntakes = dailyIntakeRepository.findAllByUserIdAndDayBetween(userId, startDate, endDate);
        List<NutrientWeight> nutrientWeights = nutrientWeightRepository.findByUserId(userId);
        for (DailyIntake dailyIntake : dailyIntakes) {
            DailyIntakeElement dailyIntakeElement = new DailyIntakeElement();
            List<NutrientValueElement> nutrientValueElements = new ArrayList<>();
            dailyIntakeElement.setDate(dailyIntake.getDay());
            for (NutrientWeight nutrientWeight : nutrientWeights) {
                NutrientValueElement nutrientValueElement = new NutrientValueElement(nutrientWeight.getNutrientName(), dailyIntake.getValue(nutrientWeight.getNutrientName()));
                nutrientValueElements.add(nutrientValueElement);
            }
            dailyIntakeElement.setNutrientValues(nutrientValueElements);
            dailyIntakeElements.add(dailyIntakeElement);
        }
        return dailyIntakeElements;
    }

    @Transactional
    public NutrientValuesDTO calcDailyIntakeScore(String userId, List<NutrientValueElement> nutrionCriterionElements) {
        int totalScore = 0;
        List<NutrientValueElement> nutrientScoreElements = new ArrayList<>();
        List<NutrientWeight> nutrientWeights = nutrientWeightRepository.findByUserId(userId);
        Map<String, NutrientWeight> weightMap = nutrientWeights.stream().collect(Collectors.toMap(
                NutrientWeight::getNutrientName,
                w -> w
        ));
        DailyIntake dailyIntake = dailyIntakeRepository.findByUserIdAndDay(userId, LocalDate.now())
                .orElseThrow(NoSuchElementException::new);
        for(NutrientValueElement nutrionCriterion : nutrionCriterionElements) {
            String nutrientName = nutrionCriterion.getNutrientName();
            int score = calculateSingleScore(dailyIntake.getValue(nutrientName), (nutrionCriterion.getValue() * weightMap.get(nutrientName).getWeight()), 1.0);
            nutrientScoreElements.add(new NutrientValueElement(nutrientName, (double)score));
            totalScore += score;
        }
        dailyIntake.setDailyscore(totalScore);
        dailyIntakeRepository.save(dailyIntake);
        return new NutrientValuesDTO(nutrientScoreElements);
    }
    private int calculateSingleScore(double actual, double target, double sigmaRatio) {
        if (target <= 0 || Double.isNaN(actual) || Double.isNaN(target)) return 0;

        double diff = actual - target;
        double sigma = Math.abs(target) * sigmaRatio;
        if (sigma == 0) return (actual == target) ? 100 : 0;

        double normalized = diff / sigma;
        double score = 100.0 * Math.exp(-0.5 * normalized * normalized);

        return (int) Math.round(Math.max(0, Math.min(100, score)));
    }

    // dailyIntake update logic

    @Transactional
    public void applyDailyIntake(MealRecord mealRecord) {
        LocalDate now = LocalDate.now();
        String userId = mealRecord.getUser().getId();
        DailyIntake dailyIntake = dailyIntakeRepository.findByUserIdAndDay(userId, now)
                .stream()
                .findFirst()
                .orElseGet(() -> dailyIntakeRepository.save(createNewDailyIntake(mealRecord.getUser(), now)));
        mealRecord.getFoodLink().forEach(foodLink -> addFoodNutrition(dailyIntake, foodLink));
    }
    private DailyIntake createNewDailyIntake(User user, LocalDate now) {
        DailyIntake dailyIntake = new DailyIntake();
        dailyIntake.setUser(user);
        dailyIntake.setDay(now);
        return dailyIntake;
    }
    private void addFoodNutrition(DailyIntake dailyIntake, MealRecordFoodLink foodLink) {
        try {
            Food food = foodLink.getFood();
            float intakeRatio = Float.parseFloat(food.getWeight().replaceAll("[^\\d.]", "")) / 100;
            intakeRatio *= foodLink.getIntakeAmount();
            System.out.println("계산된 음식중량 비율 : " + intakeRatio);
            System.out.println("이전 칼로리 섭취량 : " + dailyIntake.getEnergyKcal());
            dailyIntake.addMealIntake(
                    nullToZero(food.getEnergyKcal())*intakeRatio,
                    nullToZero(food.getCarbohydrateG())*intakeRatio,
                    nullToZero(food.getProteinG())*intakeRatio,
                    nullToZero(food.getCalciumMg())*intakeRatio,
                    nullToZero(food.getKaliumMg())*intakeRatio,
                    nullToZero(food.getIronMg())*intakeRatio,
                    nullToZero(food.getMagnesiumMg())*intakeRatio,
                    nullToZero(food.getZincMg())*intakeRatio,
                    nullToZero(food.getCelluloseG())*intakeRatio,
                    nullToZero(food.getAminoacidMg())*intakeRatio,
                    nullToZero(food.getLeucineMg())*intakeRatio,
                    nullToZero(food.getMethionineMg())*intakeRatio,
                    nullToZero(food.getSeleniumUg())*intakeRatio,
                    nullToZero(food.getOmega3G())*intakeRatio,
                    nullToZero(food.getVitaminAUg())*intakeRatio,
                    nullToZero(food.getVitaminBMg())*intakeRatio,
                    nullToZero(food.getFolicacidUg())*intakeRatio,
                    nullToZero(food.getVitaminB12Ug())*intakeRatio,
                    nullToZero(food.getVitaminCMg())*intakeRatio,
                    nullToZero(food.getVitaminDUg())*intakeRatio,
                    nullToZero(food.getVitaminEMg())*intakeRatio
            );
            System.out.println("기록된 칼로리량 : " + nullToZero(food.getEnergyKcal())*intakeRatio);
            System.out.println("기록된 단백질량 : " + nullToZero(food.getProteinG())*intakeRatio);
            System.out.println("기록된 탄수화물량 : " + nullToZero(food.getCarbohydrateG())*intakeRatio);
            System.out.println("기록된 식이섬유량 : " + nullToZero(food.getCelluloseG())*intakeRatio);
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
    public void applyDeleteDailyIntake(MealRecord mealRecord, String userId, LocalDate date) {
        DailyIntake dailyIntake = dailyIntakeRepository.findByUserIdAndDay(userId, date)
                .stream()
                .findFirst()
                .orElseThrow(NoSuchElementException::new);
        int foodNum = mealRecord.getFoods().size();
        mealRecord.getFoods().forEach(food -> deleteFoodNutrition(dailyIntake, food));
    }
    private void deleteFoodNutrition(DailyIntake dailyIntake, Food food) {
        try {
            // "200g" 등의 문자열에서 숫자만 추출하여 100g 단위 비율 계산
            // (Float보다 Double을 사용하는 것이 누적 계산 오차를 줄이는 데 유리합니다)
            double calRatio = Double.parseDouble(food.getWeight().replaceAll("[^\\d.]", "")) / 100.0;

            dailyIntake.deleteMealIntake(
                    nullToZero(food.getEnergyKcal()) * calRatio,
                    nullToZero(food.getCarbohydrateG()) * calRatio,
                    nullToZero(food.getProteinG()) * calRatio,
                    nullToZero(food.getCalciumMg()) * calRatio,
                    nullToZero(food.getKaliumMg()) * calRatio,
                    nullToZero(food.getIronMg()) * calRatio,
                    nullToZero(food.getMagnesiumMg()) * calRatio,
                    nullToZero(food.getZincMg()) * calRatio,
                    nullToZero(food.getCelluloseG()) * calRatio,
                    nullToZero(food.getAminoacidMg()) * calRatio,
                    nullToZero(food.getLeucineMg()) * calRatio,
                    nullToZero(food.getMethionineMg()) * calRatio,
                    nullToZero(food.getSeleniumUg()) * calRatio,
                    nullToZero(food.getOmega3G()) * calRatio,
                    nullToZero(food.getVitaminAUg()) * calRatio,
                    nullToZero(food.getVitaminBMg()) * calRatio,
                    nullToZero(food.getFolicacidUg()) * calRatio,
                    nullToZero(food.getVitaminB12Ug()) * calRatio,
                    nullToZero(food.getVitaminCMg()) * calRatio,
                    nullToZero(food.getVitaminDUg()) * calRatio,
                    nullToZero(food.getVitaminEMg()) * calRatio
            );
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
