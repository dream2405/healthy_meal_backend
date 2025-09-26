package kr.ac.dankook.ace.healthy_meal_backend.service;

import kr.ac.dankook.ace.healthy_meal_backend.entity.DailyIntake;
import kr.ac.dankook.ace.healthy_meal_backend.entity.DietCriterion; // 기존 DietCriterion (연령/성별별 기준)
import kr.ac.dankook.ace.healthy_meal_backend.entity.User;
import kr.ac.dankook.ace.healthy_meal_backend.repository.DailyIntakeRepository;
// 기존 DietCriterionRepository 주입
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DietaryScoreService {

    private final DailyIntakeRepository dailyIntakeRepository;
    private final UserService userService;

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void updateDailyIntakeScore() {
        try {
            List<DailyIntake> allDailyIntakes = dailyIntakeRepository.findByDay(LocalDate.now().minusDays(1));
            for (DailyIntake dailyIntake : allDailyIntakes) {
                dailyIntake.setScore(calculateScoreFromDailyIntake(dailyIntake));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int calculateScoreFromDailyIntake(DailyIntake dailyIntake) {
        // 실제섭취량 리스트
        List<Double> actuals = List.of(
                dailyIntake.getEnergyKcal(),
                dailyIntake.getCarbohydrateG(),
                dailyIntake.getFatG(),
                dailyIntake.getProteinG(),
                dailyIntake.getCelluloseG(),
                dailyIntake.getSugarsG(),
                dailyIntake.getSodiumMg(),
                dailyIntake.getCholesterolMg()
        );
        // 권장섭취량 리스트
        User user = dailyIntake.getUser();
        int age = Period.between(user.getBirthday(), LocalDate.now()).getYears();
        DietCriterion dietCriterion = userService.applyWeight(user.getId(), age, user.getGender());
        List<Float> targets = List.of(
                dietCriterion.getEnergyKcal(),
                dietCriterion.getCarbohydrateG(),
                dietCriterion.getFatG(),
                dietCriterion.getProteinG(),
                dietCriterion.getCelluloseG(),
                dietCriterion.getSugarsG(),
                dietCriterion.getSodiumMg(),
                dietCriterion.getCholesterolMg()
        );

        double totalScore = 0;
        double maxScore = 0;
        for (int i = 0; i < targets.size(); i++) {
            double actualValue = actuals.get(i);
            double targetValue = targets.get(i);
            double nutrientScore = calculateScorePerNutrient(actualValue, targetValue);
            totalScore += nutrientScore;
            maxScore += 100;
        }

        return (int) Math.round((totalScore / maxScore)*100);
    }
    private double calculateScorePerNutrient(double actual, double target) {
        if (target == 0) return 100.0;
        double ratio = actual / target;
        double deviation = Math.abs(1.0 - ratio);
        double penalty = deviation * 100;
        double score = 100.0 - penalty;
        return Math.max(0, score);
    }
}
