package kr.ac.dankook.ace.healthy_meal_backend.repository;

import kr.ac.dankook.ace.healthy_meal_backend.entity.MealInfo;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MealInfoRepository extends CrudRepository<MealInfo, Long> {
    @Query("SELECT m FROM MealInfo m WHERE m.user.id = :userId AND (:date IS NULL OR DATE(m.createdAt) = :date)")
    List<MealInfo> findByUserIdAndCreatedDate(
            @Param("userId") String userId,
            @Param("date") LocalDate date);
}
