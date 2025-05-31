package kr.ac.dankook.ace.healthy_meal_backend.repository;

import kr.ac.dankook.ace.healthy_meal_backend.entity.DailyIntake;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DailyIntakeRepository extends CrudRepository<DailyIntake, Integer> {
    List<DailyIntake> findByUserIdAndDay(String userId, LocalDate day);

    boolean existsByUserIdAndId(String userId, Integer id);

    List<DailyIntake> findByUserId(String userId);
}
