package kr.ac.dankook.ace.healthy_meal_backend.repository;

import kr.ac.dankook.ace.healthy_meal_backend.entity.MealRecord;
import kr.ac.dankook.ace.healthy_meal_backend.entity.MealRecordFoodLink;
import kr.ac.dankook.ace.healthy_meal_backend.entity.MealRecordFoodLinkId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MealRecordFoodLinkRepository extends JpaRepository<MealRecordFoodLink, MealRecordFoodLinkId> {
    List<MealRecordFoodLink> findByMealRecord(MealRecord mealRecord);
}
