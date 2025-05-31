package kr.ac.dankook.ace.healthy_meal_backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 각 영양소별 식단 점수 계산 규칙 및 권장량 비율을 정의하는 엔티티입니다.
 * 이 엔티티는 DietaryScoreService에서 각 영양소의 점수를 계산할 때 사용되는 기준값을 제공합니다.
 * nutrientName 필드를 통해 특정 영양소의 점수 계산 기준 정보를 조회할 수 있습니다.
 * 기존의 연령/성별 기반 DietCriterion과는 별도로 식단 점수 계산 로직에 특화된 기준을 관리합니다.
 */
@Entity
@Table(name = "diet_scoring_criterion") // 테이블 이름도 명확하게 변경 (예: diet_criterion_scoring -> diet_scoring_criterion)
@Getter
@Setter
@NoArgsConstructor
public class DietScoringCriterion { // 클래스 이름 변경: DietCriterion -> DietScoringCriterion

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 영양소 한글 이름 (NutrientType의 koreanName과 매칭).
     * 이 필드는 UNIQUE 제약 조건을 가질 수 있습니다.
     */
    @Column(name = "nutrient_name", nullable = false, unique = true)
    private String nutrientName;

    /**
     * 해당 영양소의 일반적인 권장 섭취량.
     * (예: 탄수화물 130g, 단백질 65g 등)
     * 이 값은 DietaryScoreService에서 기본 권장량으로 사용될 수 있으며,
     * NutrientType enum의 defaultRecommendedAmount와 함께 고려됩니다.
     * 보다 정교한 시스템에서는 이 엔티티가 사용자의 특성(연령, 성별 등)을 반영한
     * 다양한 프로파일별 기준을 가질 수도 있습니다.
     */
    @Column(name = "recommended_amount")
    private Double recommendedAmount;

    /**
     * 점수 계산 시 최적 범위의 최소 비율 (예: 권장량의 80% -> 0.8).
     * NutrientScoringType이 TARGET_RANGE 또는 TARGET_RANGE_UPPER_SENSITIVE 일 때 사용됩니다.
     */
    @Column(name = "min_optimal_ratio")
    private Double minOptimalRatio;

    /**
     * 점수 계산 시 최적 범위의 최대 비율 (예: 권장량의 120% -> 1.2).
     * NutrientScoringType이 TARGET_RANGE 또는 TARGET_RANGE_UPPER_SENSITIVE 일 때 사용됩니다.
     */
    @Column(name = "max_optimal_ratio")
    private Double maxOptimalRatio;

    /**
     * 과다 섭취 시 감점이 시작되는 비율 (예: 권장량의 150% -> 1.5).
     * NutrientScoringType이 TARGET_RANGE, TARGET_RANGE_UPPER_SENSITIVE, LESS_IS_BETTER 일 때 사용될 수 있습니다.
     */
    @Column(name = "penalty_start_ratio_upper")
    private Double penaltyStartRatioUpper;

    /**
     * 과다 섭취 시 점수가 0점이 되는 비율 (예: 권장량의 200% -> 2.0).
     * NutrientScoringType이 TARGET_RANGE, TARGET_RANGE_UPPER_SENSITIVE, LESS_IS_BETTER 일 때 사용될 수 있습니다.
     */
    @Column(name = "zero_score_ratio_upper")
    private Double zeroScoreRatioUpper;

    // 참고: 이 엔티티는 식단 점수 계산 로직을 위한 파라미터를 저장합니다.
    // 기존에 업로드해주신 파일 목록에 있던 DietCriterion 엔티티는
    // (kr.ac.dankook.ace.healthy_meal_backend.entity.DietCriterion.java)
    // 주로 연령 및 성별에 따른 영양소 섭취 '목표치'를 저장하는 용도로 계속 사용될 수 있습니다.
    // 두 엔티티는 서로 다른 목적을 가지므로, 클래스 이름과 테이블 이름을 구분하여 함께 사용할 수 있습니다.
}

