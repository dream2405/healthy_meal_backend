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

    @Query(value = "SELECT DISTINCT major_category FROM food WHERE major_category IS NOT NULL",
            nativeQuery = true)
    List<String> findDistinctMajorCategoryNative();

    @Query(value = "SELECT DISTINCT representative_food FROM food WHERE major_category = :majorCategory AND representative_food IS NOT NULL",
            nativeQuery = true)
    List<String> findDistinctRepresentativeFoodByMajorCategory(@Param("majorCategory") String majorCategory);

    @Query(value = "SELECT DISTINCT name FROM food WHERE representative_food = :representativeFood",
            nativeQuery = true)
    List<String> findDistinctNameByRepresentativeFood(@Param("representativeFood") String representativeFood);
}
