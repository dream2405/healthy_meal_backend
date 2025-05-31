package kr.ac.dankook.ace.healthy_meal_backend.model.enums;

import java.util.List;
import java.util.Optional;

/**
 * 각 영양소의 이름, 단위, 기본 권장량, 점수 계산 방식(NutrientScoringType), 기본 중요도(baseImportance)를 정의하는 Enum 클래스입니다.
 */
public enum NutrientType {
    CARBOHYDRATE("탄수화물", "g", 130.0, NutrientScoringType.TARGET_RANGE, 1.0),
    PROTEIN("단백질", "g", 65.0, NutrientScoringType.ENOUGH_IS_GOOD, 1.0),
    FAT("지방", "g", 51.0, NutrientScoringType.TARGET_RANGE_UPPER_SENSITIVE, 1.0), // 예시 권장량, 실제로는 성별/나이별 기준 필요
    SODIUM("나트륨", "mg", 2000.0, NutrientScoringType.LESS_IS_BETTER, 0.8),
    CELLULOSE("식이섬유", "g", 25.0, NutrientScoringType.ENOUGH_IS_GOOD, 1.0),
    SUGARS("당류", "g", 50.0, NutrientScoringType.LESS_IS_BETTER, 0.8),
    CHOLESTEROL("콜레스테롤", "mg", 300.0, NutrientScoringType.LESS_IS_BETTER, 0.8),
    ENERGY("에너지", "kcal", 2000.0, NutrientScoringType.TARGET_RANGE, 1.0);

    private final String koreanName;
    private final String unit;
    private final double defaultRecommendedAmount; // 기본 권장량 (DietCriterion에서 가져오는 것을 우선)
    private final NutrientScoringType scoringType;
    private final double baseImportance; // 영양소별 기본 중요도 (0.0 ~ 1.0+)

    NutrientType(String koreanName, String unit, double defaultRecommendedAmount, NutrientScoringType scoringType, double baseImportance) {
        this.koreanName = koreanName;
        this.unit = unit;
        this.defaultRecommendedAmount = defaultRecommendedAmount;
        this.scoringType = scoringType;
        this.baseImportance = baseImportance;
    }

    public String getKoreanName() { return koreanName; }
    public String getUnit() { return unit; }
    public double getDefaultRecommendedAmount() { return defaultRecommendedAmount; }
    public NutrientScoringType getScoringType() { return scoringType; }
    public double getBaseImportance() { return baseImportance; }

    /**
     * 한글 영양소 이름으로 NutrientType Enum 상수를 찾습니다.
     * @param name 찾고자 하는 영양소의 한글 이름
     * @return Optional<NutrientType> 객체, 찾지 못한 경우 Optional.empty()
     */
    public static Optional<NutrientType> fromKoreanName(String name) {
        for (NutrientType nt : values()) {
            if (nt.koreanName.equals(name)) {
                return Optional.of(nt);
            }
        }
        return Optional.empty();
    }

    /**
     * 모든 NutrientType Enum 상수 리스트를 반환합니다.
     * @return NutrientType 상수 리스트
     */
    public static List<NutrientType> valuesList() {
        return List.of(values());
    }
}
