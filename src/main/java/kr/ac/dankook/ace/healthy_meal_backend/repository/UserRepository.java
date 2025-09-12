package kr.ac.dankook.ace.healthy_meal_backend.repository;

import kr.ac.dankook.ace.healthy_meal_backend.entity.User;

import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends CrudRepository<User, String> {
    @NotNull Optional<User> findById(@NotNull String userId);
}
