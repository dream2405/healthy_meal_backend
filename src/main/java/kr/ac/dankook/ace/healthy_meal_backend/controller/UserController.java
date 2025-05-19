package kr.ac.dankook.ace.healthy_meal_backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.ac.dankook.ace.healthy_meal_backend.entity.MealInfo;
import kr.ac.dankook.ace.healthy_meal_backend.entity.User;
import kr.ac.dankook.ace.healthy_meal_backend.entity.UserPostDTO;
import kr.ac.dankook.ace.healthy_meal_backend.repository.MealInfoRepository;
import kr.ac.dankook.ace.healthy_meal_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("/users")
@Tag(name = "유저")
public class UserController {
    private final UserRepository userRepository;
    private final MealInfoRepository mealInfoRepository;

    @Autowired
    public UserController(UserRepository userRepository, MealInfoRepository mealInfoRepository) {
        this.userRepository = userRepository;
        this.mealInfoRepository = mealInfoRepository;
    }

    @PostMapping
    @Operation(summary = "주어진 정보로 회원가입")
    public ResponseEntity<User> createUser(@RequestBody UserPostDTO userPost) {
        if (userRepository.existsById(userPost.getId())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        User user = new User();
        user.setId(userPost.getId());
        user.setHashedPassword(userPost.getHashedPassword());
        user.setBirthday(userPost.getBirthday());
        user.setGender(userPost.getGender());

        var savedUser = userRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
    }

    @GetMapping("/{userId}")
    @Operation(summary = "주어진 ID를 가진 특정 유저 가져오기")
    public ResponseEntity<User> getUser(@PathVariable String userId) {
        Optional<User> user = userRepository.findById(userId);
        return user
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @DeleteMapping("/{userId}")
    @Operation(summary = "주어진 ID를 가진 유저 삭제")
    public ResponseEntity<User> deleteUser(@PathVariable String userId) {
        if (!userRepository.existsById(userId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        userRepository.deleteById(userId);
        return ResponseEntity.status(HttpStatus.valueOf(204)).build();
    }

    @GetMapping("/{userId}/meal-info")
    @Operation(summary = "주어진 ID의 유저가 기록한 모든 식단 정보 가져오기")
    public ResponseEntity<List<MealInfo>> getMealInfo(@PathVariable String userId) {
        Optional<User> user = userRepository.findById(userId);
        return user
                .map(value -> ResponseEntity.status(HttpStatus.OK).body(value.getMealInfos()))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PostMapping("/{userId}/meal-info")
    @Operation(summary = "주어진 정보로 주어진 ID의 유저가 식단정보 기록")
    public ResponseEntity<MealInfo> createMealInfo(@PathVariable String userId, @RequestBody MealInfo mealInfo) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        user.get().addMealInfo(mealInfo);
        return ResponseEntity.status(HttpStatus.CREATED).body(mealInfo);
    }

    @GetMapping("/{userId}/meal-info/{mealInfoId}")
    @Operation(summary = "주어진 ID의 유저가 기록한 주어진 ID의 식단 정보 가져오기")
    public ResponseEntity<MealInfo> getMealInfo(@PathVariable String userId, @PathVariable Long mealInfoId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        Optional<MealInfo> mealInfo = user.get().getMealInfos().stream().filter(mf -> Objects.equals(mf.getId(), mealInfoId)).findFirst();
        return mealInfo
                .map(info -> ResponseEntity.status(HttpStatus.OK).body(info))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }


}
