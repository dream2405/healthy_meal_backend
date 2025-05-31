package kr.ac.dankook.ace.healthy_meal_backend.repository;

import kr.ac.dankook.ace.healthy_meal_backend.entity.DailyIntake;
import kr.ac.dankook.ace.healthy_meal_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface DailyIntakeRepository extends JpaRepository<DailyIntake, Integer> { // ID 타입 Integer로 가정

    /**
     * 특정 사용자의 특정 날짜에 대한 일일 섭취 기록을 조회합니다.
     * DietaryScoreService에서 기존 기록을 찾아 업데이트하거나 새로 생성할 때 사용됩니다.
     * @param userId 조회할 사용자의 ID
     * @param day 조회할 날짜
     * @return Optional<DailyIntake> 객체
     */
    Optional<DailyIntake> findByUserIdAndDay(String userId, LocalDate day);

    // User 엔티티 객체로도 조회 가능하도록 오버로딩 할 수 있습니다.
    // Optional<DailyIntake> findByUserAndDay(User user, LocalDate day);
}
