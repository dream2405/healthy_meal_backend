package kr.ac.dankook.ace.healthy_meal_backend.repository;

import kr.ac.dankook.ace.healthy_meal_backend.entity.Food;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FoodRepository extends CrudRepository<Food, Long> {
    List<Food> findAllByName(String name);

    @Query("SELECT f FROM Food f WHERE " +
            "(:name IS NULL OR f.name=:name) AND " +
            "(:representativeFood IS NULL OR f.representativeFood=:representativeFood) AND " +
            "(:majorCategory IS NULL OR f.majorCategory=:majorCategory)"
    )
    List<Food> findByDynamicConditions(
            @Param("name") String name,
            @Param("representativeFood") String representativeFood,
            @Param("majorCategory") String majorCategory
    );
}
