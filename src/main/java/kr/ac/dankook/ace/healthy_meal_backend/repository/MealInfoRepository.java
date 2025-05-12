package kr.ac.dankook.ace.healthy_meal_backend.repository;

import kr.ac.dankook.ace.healthy_meal_backend.entity.MealInfo;
import org.springframework.data.repository.CrudRepository;

public interface MealInfoRepository extends CrudRepository<MealInfo, Long> {
}
