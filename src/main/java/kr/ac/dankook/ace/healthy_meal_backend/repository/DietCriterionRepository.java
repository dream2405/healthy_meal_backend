package kr.ac.dankook.ace.healthy_meal_backend.repository;

import kr.ac.dankook.ace.healthy_meal_backend.entity.DietCriterion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 사용자의 연령 및 성별에 따른 일일 영양소 섭취 기준 (DietCriterion 엔티티)을 위한 Repository 인터페이스입니다.
 * 이 리포지토리는 기존의 연령/성별별 영양 목표치를 관리합니다.
 */
@Repository
public interface DietCriterionRepository extends JpaRepository<DietCriterion, Integer> { // CrudRepository에서 JpaRepository로 변경

    /**
     * 주어진 나이와 성별에 해당하는 DietCriterion을 조회합니다.
     * 나이는 startAge와 endAge 사이에 있어야 하며, 성별이 일치해야 합니다.
     *
     * @param age 사용자의 현재 나이
     * @param gender 사용자의 성별 ('M', 'F')
     * @return 해당 조건에 맞는 DietCriterion (Optional)
     */
    @Query("SELECT dc FROM DietCriterion dc WHERE :age >= dc.startAge AND :age <= dc.endAge AND dc.gender = :gender")
    Optional<DietCriterion> findApplicableCriterion(@Param("age") Integer age, @Param("gender") Character gender);
}
