package kr.ac.dankook.ace.healthy_meal_backend.model.enums;

/**
 * 영양소별 점수 계산 방식을 정의하는 Enum 클래스입니다.
 */
public enum NutrientScoringType {
    /**
     * 목표 범위 (예: 칼로리, 탄수화물, 지방) - 너무 적거나 많으면 감점
     */
    TARGET_RANGE,
    /**
     * 충분히 섭취하는 것이 좋음 (예: 식이섬유, 단백질) - 권장량 이상이면 만점
     */
    ENOUGH_IS_GOOD,
    /**
     * 적게 섭취하는 것이 좋음 (예: 나트륨, 당류, 콜레스테롤) - 권장 상한치 이하이면 만점
     */
    LESS_IS_BETTER,
    /**
     * 목표 범위이지만 상한 초과에 더 민감 (예: 지방)
     */
    TARGET_RANGE_UPPER_SENSITIVE
}

