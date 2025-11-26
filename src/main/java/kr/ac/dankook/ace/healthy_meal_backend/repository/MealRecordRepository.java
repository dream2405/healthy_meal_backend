package kr.ac.dankook.ace.healthy_meal_backend.repository;

import kr.ac.dankook.ace.healthy_meal_backend.entity.MealRecord;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MealRecordRepository extends CrudRepository<MealRecord, Long> {
    @Query("SELECT m FROM MealRecord m WHERE m.user.id = :userId AND (:date IS NULL OR DATE(m.createdAt) = :date)")
    List<MealRecord> findByUserIdAndCreatedDate(
            @Param("userId") String userId,
            @Param("date") LocalDate date);
}
