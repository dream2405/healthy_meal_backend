package kr.ac.dankook.ace.healthy_meal_backend.repository;

import kr.ac.dankook.ace.healthy_meal_backend.entity.DietScoringCriterion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * DietScoringCriterion 엔티티를 위한 Repository 인터페이스입니다.
 * 식단 점수 계산에 사용되는 영양소별 기준 정보를 데이터베이스에서 조회합니다.
 */
@Repository
public interface DietScoringCriterionRepository extends JpaRepository<DietScoringCriterion, Long> {
    /**
     * 영양소 이름으로 특정 DietScoringCriterion을 조회합니다.
     * DietaryScoreService에서 각 영양소별 점수 계산 기준을 가져올 때 사용됩니다.
     * @param nutrientName 조회할 영양소의 이름 (NutrientType.koreanName과 일치)
     * @return Optional<DietScoringCriterion> 객체
     */
    Optional<DietScoringCriterion> findByNutrientName(String nutrientName);

    // findAll() 메소드는 JpaRepository에서 기본으로 제공됩니다.
    // DietaryScoreService에서는 findAll()을 사용하여 모든 점수 기준을 가져온 후
    // 스트림을 통해 필터링하고 Map으로 변환하여 사용합니다.
}
