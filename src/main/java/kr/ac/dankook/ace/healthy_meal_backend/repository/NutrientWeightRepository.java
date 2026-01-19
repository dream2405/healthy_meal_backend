package kr.ac.dankook.ace.healthy_meal_backend.repository;

import kr.ac.dankook.ace.healthy_meal_backend.entity.NutrientWeight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NutrientWeightRepository extends JpaRepository<NutrientWeight, Long> { // ID 타입 Integer로 가정

    /**
     * 특정 사용자가 설정한 모든 영양소 가중치 정보를 조회합니다.
     * DietaryScoreService에서 사용자별 맞춤 점수를 계산할 때 사용됩니다.
     * @param userId 조회할 사용자의 ID
     * @return 해당 사용자의 NutriWeight 리스트
     */
    List<NutrientWeight> findByUserId(String userId);

    Optional<NutrientWeight> findByUserIdAndNutrientName(String userId, String nutrientName);

    void deleteByUserId(String userId);
}
