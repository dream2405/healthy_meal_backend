package kr.ac.dankook.ace.healthy_meal_backend.service;

import kr.ac.dankook.ace.healthy_meal_backend.entity.DailyIntake;
import kr.ac.dankook.ace.healthy_meal_backend.entity.DietCriterion; // 기존 DietCriterion (연령/성별별 기준)
import kr.ac.dankook.ace.healthy_meal_backend.entity.DietScoringCriterion;
import kr.ac.dankook.ace.healthy_meal_backend.entity.NutriWeight;
import kr.ac.dankook.ace.healthy_meal_backend.entity.User;
import kr.ac.dankook.ace.healthy_meal_backend.model.enums.NutrientType;
import kr.ac.dankook.ace.healthy_meal_backend.repository.DailyIntakeRepository;
import kr.ac.dankook.ace.healthy_meal_backend.repository.DietCriterionRepository; // 기존 DietCriterionRepository 주입
import kr.ac.dankook.ace.healthy_meal_backend.repository.DietScoringCriterionRepository;
import kr.ac.dankook.ace.healthy_meal_backend.repository.NutriWeightRepository;
import kr.ac.dankook.ace.healthy_meal_backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DietaryScoreService {

    private static final Logger logger = LoggerFactory.getLogger(DietaryScoreService.class);

    private final DietScoringCriterionRepository dietScoringCriterionRepository;
    private final NutriWeightRepository nutriWeightRepository;
    private final DailyIntakeRepository dailyIntakeRepository;
    private final UserRepository userRepository;
    private final DietCriterionRepository dietCriterionRepository; // 기존 DietCriterionRepository 주입

    public DietaryScoreService(DietScoringCriterionRepository dietScoringCriterionRepository,
                               NutriWeightRepository nutriWeightRepository,
                               DailyIntakeRepository dailyIntakeRepository,
                               UserRepository userRepository,
                               DietCriterionRepository dietCriterionRepository) { // 생성자에 추가
        this.dietScoringCriterionRepository = dietScoringCriterionRepository;
        this.nutriWeightRepository = nutriWeightRepository;
        this.dailyIntakeRepository = dailyIntakeRepository;
        this.userRepository = userRepository;
        this.dietCriterionRepository = dietCriterionRepository; // 의존성 주입
    }

    @Transactional
    public DailyIntake calculateScoreFromDailyIntake(String userId, LocalDate date) {
        logger.info("Calculating personalized dietary score for userId: {} on date: {}", userId, date);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        if (dailyIntakeRepository.findByUserIdAndDay(userId, date).isEmpty()) {
            throw new IllegalArgumentException("DailyIntake record not found for user " + userId + " on " + date);
        }
        DailyIntake dailyIntake = dailyIntakeRepository.findByUserIdAndDay(userId, date).get(0);

        Map<NutrientType, Double> actualIntakes = extractActualIntakesFromDailyIntake(dailyIntake);
        logger.debug("Extracted actual intakes from DailyIntake: {}", actualIntakes);

        // 사용자의 나이 계산
        int age = 0;
        if (user.getBirthday() != null) {
            age = Period.between(user.getBirthday(), LocalDate.now()).getYears();
        }
        logger.debug("User age: {}", age);

        // 사용자의 연령/성별에 맞는 DietCriterion (섭취 목표치) 조회
        Optional<DietCriterion> userSpecificDietCriterionOpt = Optional.empty();
        if (user.getGender() != null && age > 0) {
            userSpecificDietCriterionOpt = dietCriterionRepository.findApplicableCriterion(age, user.getGender());
            if (userSpecificDietCriterionOpt.isPresent()) {
                logger.debug("Found user-specific DietCriterion (age/gender based targets).");
            } else {
                logger.debug("User-specific DietCriterion (age/gender based targets) not found for age {} and gender {}.", age, user.getGender());
            }
        }


        Map<NutrientType, DietScoringCriterion> dietScoringCriteriaMap = dietScoringCriterionRepository.findAll()
                .stream()
                .filter(dsc -> NutrientType.fromKoreanName(dsc.getNutrientName()).isPresent())
                .collect(Collectors.toMap(
                        dsc -> NutrientType.fromKoreanName(dsc.getNutrientName()).get(),
                        dsc -> dsc,
                        (e, r) -> e
                ));
        logger.debug("Loaded {} diet scoring criteria.", dietScoringCriteriaMap.size());

        Map<NutrientType, NutriWeight> userNutriWeightsMap = nutriWeightRepository.findByUserId(userId)
                .stream()
                .filter(nw -> NutrientType.fromKoreanName(nw.getNutrient()).isPresent())
                .collect(Collectors.toMap(
                        nw -> NutrientType.fromKoreanName(nw.getNutrient()).get(),
                        nw -> nw
                ));
        logger.debug("Loaded {} user nutrient weights.", userNutriWeightsMap.size());

        double totalWeightedScore = 0;
        double maxPossibleTotalWeightedScore = 0;

        for (NutrientType nutrient : NutrientType.valuesList()) {
            double actualIntake = actualIntakes.getOrDefault(nutrient, 0.0);
            DietScoringCriterion scoringCriterion = dietScoringCriteriaMap.get(nutrient);

            // 권장 섭취량 결정 (우선순위: 사용자 맞춤 DietCriterion -> DietScoringCriterion -> NutrientType 기본값)
            double recommendedAmount;
            Optional<Double> userSpecificRecommendedOpt = userSpecificDietCriterionOpt
                    .flatMap(crit -> getNutrientValueFromDietCriterion(crit, nutrient));

            if (userSpecificRecommendedOpt.isPresent()) {
                recommendedAmount = userSpecificRecommendedOpt.get();
                logger.trace("Using user-specific recommended amount for {}: {}", nutrient.getKoreanName(), recommendedAmount);
            } else if (scoringCriterion != null && scoringCriterion.getRecommendedAmount() != null) {
                recommendedAmount = scoringCriterion.getRecommendedAmount();
                logger.trace("Using DietScoringCriterion recommended amount for {}: {}", nutrient.getKoreanName(), recommendedAmount);
            } else {
                recommendedAmount = nutrient.getDefaultRecommendedAmount();
                logger.trace("Using NutrientType default recommended amount for {}: {}", nutrient.getKoreanName(), recommendedAmount);
            }

            double partialPerformanceScore = calculatePartialPerformanceScore(
                    nutrient,
                    actualIntake,
                    recommendedAmount,
                    scoringCriterion
            );

            double userWeightFactor = 1.0;
            if (userNutriWeightsMap.containsKey(nutrient) && userNutriWeightsMap.get(nutrient).getWeight() != null) {
                userWeightFactor = 1.0 + userNutriWeightsMap.get(nutrient).getWeight();
            }
            userWeightFactor = Math.max(0.1, userWeightFactor);

            double baseImportance = nutrient.getBaseImportance();
            double weightedNutrientScore = partialPerformanceScore * userWeightFactor * baseImportance;
            totalWeightedScore += weightedNutrientScore;
            maxPossibleTotalWeightedScore += 1.0 * userWeightFactor * baseImportance;

            logger.trace("Nutrient: {}, Actual: {:.2f}, Recommended: {:.2f}, PPS: {:.2f}, WeightFactor: {:.2f}, Importance: {:.2f}, WeightedScore: {:.2f}",
                    nutrient.getKoreanName(), actualIntake, recommendedAmount, partialPerformanceScore, userWeightFactor, baseImportance, weightedNutrientScore);
        }

        int finalScore = 0;
        if (maxPossibleTotalWeightedScore > 0) {
            finalScore = (int) Math.round((totalWeightedScore / maxPossibleTotalWeightedScore) * 100);
        }
        finalScore = Math.max(0, Math.min(100, finalScore));
        logger.info("Calculated final personalized score: {} for userId: {}", finalScore, userId);

        dailyIntake.setUser(user); // User가 설정 안되어 있을 경우를 대비
        dailyIntake.setScore(finalScore);

        DailyIntake savedDailyIntake = dailyIntakeRepository.save(dailyIntake);
        logger.info("Updated DailyIntake with id: {} with new personalized score: {}", savedDailyIntake.getId(), savedDailyIntake.getScore());
        return savedDailyIntake;
    }

    private Map<NutrientType, Double> extractActualIntakesFromDailyIntake(DailyIntake dailyIntake) {
        Map<NutrientType, Double> actualIntakes = new EnumMap<>(NutrientType.class);
        actualIntakes.put(NutrientType.ENERGY, Optional.ofNullable(dailyIntake.getEnergyKcal()).map(Integer::doubleValue).orElse(0.0));
        actualIntakes.put(NutrientType.PROTEIN, Optional.ofNullable(dailyIntake.getProteinG()).map(Float::doubleValue).orElse(0.0));
        actualIntakes.put(NutrientType.FAT, Optional.ofNullable(dailyIntake.getFatG()).map(Float::doubleValue).orElse(0.0));
        actualIntakes.put(NutrientType.CARBOHYDRATE, Optional.ofNullable(dailyIntake.getCarbohydrateG()).map(Float::doubleValue).orElse(0.0));
        actualIntakes.put(NutrientType.SUGARS, Optional.ofNullable(dailyIntake.getSugarsG()).map(Float::doubleValue).orElse(0.0));
        actualIntakes.put(NutrientType.CELLULOSE, Optional.ofNullable(dailyIntake.getCelluloseG()).map(Float::doubleValue).orElse(0.0));
        actualIntakes.put(NutrientType.SODIUM, Optional.ofNullable(dailyIntake.getSodiumMg()).map(Float::doubleValue).orElse(0.0));
        actualIntakes.put(NutrientType.CHOLESTEROL, Optional.ofNullable(dailyIntake.getCholesterolMg()).map(Float::doubleValue).orElse(0.0));
        return actualIntakes;
    }

    /**
     * DietCriterion (연령/성별별 기준) 객체에서 특정 NutrientType에 해당하는 영양소 값을 추출합니다.
     * @param criterion 사용자의 연령/성별에 맞는 DietCriterion 객체
     * @param nutrient  값을 추출할 NutrientType
     * @return 해당 영양소의 권장량 (Optional<Double>)
     */
    private Optional<Double> getNutrientValueFromDietCriterion(DietCriterion criterion, NutrientType nutrient) {
        if (criterion == null) return Optional.empty();
        Float value;
        Integer intValue;

        switch (nutrient) {
            case ENERGY:
                intValue = criterion.getEnergyKcal(); // Integer 타입
                return Optional.ofNullable(intValue).map(Integer::doubleValue);
            case PROTEIN:
                value = criterion.getProteinG();
                break;
            case FAT:
                value = criterion.getFatG();
                break;
            case CARBOHYDRATE:
                value = criterion.getCarbohydrateG();
                break;
            case SUGARS:
                value = criterion.getSugarsG();
                break;
            case CELLULOSE:
                value = criterion.getCelluloseG();
                break;
            case SODIUM:
                value = criterion.getSodiumMg();
                break;
            case CHOLESTEROL:
                value = criterion.getCholesterolMg();
                break;
            default:
                return Optional.empty();
        }
        return Optional.ofNullable(value).map(Float::doubleValue);
    }


    private double calculatePartialPerformanceScore(NutrientType nutrient, double actualIntake, double recommendedAmount, DietScoringCriterion scoringCriterion) {
        // 이 메소드의 내용은 이전 버전과 동일하게 유지됩니다.
        // (필요시 로깅이나 예외 처리 보강 가능)
        if (recommendedAmount <= 0) {
            logger.warn("Recommended amount for {} is zero or negative ({}), returning default score 0.5", nutrient.getKoreanName(), recommendedAmount);
            return 0.5;
        }

        double minOptimalRatio = (scoringCriterion != null && scoringCriterion.getMinOptimalRatio() != null) ? scoringCriterion.getMinOptimalRatio() : 0.8;
        double maxOptimalRatio = (scoringCriterion != null && scoringCriterion.getMaxOptimalRatio() != null) ? scoringCriterion.getMaxOptimalRatio() : 1.2;
        // double defaultPenaltyStartRatioUpper = (scoringCriterion != null && scoringCriterion.getPenaltyStartRatioUpper() != null) ? scoringCriterion.getPenaltyStartRatioUpper() : 1.5;
        // double defaultZeroScoreRatioUpper = (scoringCriterion != null && scoringCriterion.getZeroScoreRatioUpper() != null) ? scoringCriterion.getZeroScoreRatioUpper() : 2.0;

        double ratio = actualIntake / recommendedAmount;

        switch (nutrient.getScoringType()) {
            case TARGET_RANGE:
                if (ratio >= minOptimalRatio && ratio <= maxOptimalRatio) return 1.0;
                if (ratio < minOptimalRatio) return Math.max(0, ratio / minOptimalRatio);
                double zeroScoreUpperTR = (scoringCriterion != null && scoringCriterion.getZeroScoreRatioUpper() != null) ? scoringCriterion.getZeroScoreRatioUpper() : 2.0;
                if (zeroScoreUpperTR <= maxOptimalRatio) zeroScoreUpperTR = maxOptimalRatio + 0.1;
                return Math.max(0, 1.0 - (ratio - maxOptimalRatio) / (zeroScoreUpperTR - maxOptimalRatio));

            case TARGET_RANGE_UPPER_SENSITIVE:
                 if (ratio >= minOptimalRatio && ratio <= maxOptimalRatio) return 1.0;
                 if (ratio < minOptimalRatio) return Math.max(0, ratio / minOptimalRatio);
                 double penaltyStartUpperTRUS = (scoringCriterion != null && scoringCriterion.getPenaltyStartRatioUpper() != null) ? scoringCriterion.getPenaltyStartRatioUpper() : 1.5;
                 if (penaltyStartUpperTRUS <= maxOptimalRatio) penaltyStartUpperTRUS = maxOptimalRatio + 0.1;
                 return Math.max(0, 1.0 - (ratio - maxOptimalRatio) / (penaltyStartUpperTRUS - maxOptimalRatio));

            case ENOUGH_IS_GOOD:
                return Math.min(1.0, ratio);

            case LESS_IS_BETTER:
                if (ratio <= 1.0) return 1.0;
                double zeroLimitLIB = (scoringCriterion != null && scoringCriterion.getZeroScoreRatioUpper() != null) ? scoringCriterion.getZeroScoreRatioUpper() : 1.5;
                if (zeroLimitLIB <= 1.0) {
                    logger.warn("zeroScoreRatioUpper for LESS_IS_BETTER nutrient {} is invalid ({}). Using default 1.5.", nutrient.getKoreanName(), zeroLimitLIB);
                    zeroLimitLIB = 1.5;
                }
                return Math.max(0, 1.0 - (ratio - 1.0) / (zeroLimitLIB - 1.0));

            default:
                logger.warn("Unknown scoring type for nutrient {}: {}. Returning default score 0.5", nutrient.getKoreanName(), nutrient.getScoringType());
                return 0.5;
        }
    }
}
