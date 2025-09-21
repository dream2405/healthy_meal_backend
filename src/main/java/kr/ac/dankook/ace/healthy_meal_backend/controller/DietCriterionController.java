package kr.ac.dankook.ace.healthy_meal_backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.ac.dankook.ace.healthy_meal_backend.repository.UserRepository;
import kr.ac.dankook.ace.healthy_meal_backend.service.UserService;
import kr.ac.dankook.ace.healthy_meal_backend.dto.DietCriterionWeightDTO;
import kr.ac.dankook.ace.healthy_meal_backend.entity.DietCriterion;
import kr.ac.dankook.ace.healthy_meal_backend.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;
import java.util.Optional;

@RestController
@RequestMapping("/diet-criteria")
@Tag(name = "식단기준")
public class DietCriterionController {

    private final UserService userService;
    private final UserRepository userRepository;

    @Autowired
    public DietCriterionController(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @Operation(summary = "사용자별 영양소 섭취기준 조회", security = @SecurityRequirement(name = "BearerAuth"))
    @GetMapping("/{userId}")
    public ResponseEntity<DietCriterion> getDietCriteria(
            @RequestParam Integer age, @RequestParam char gender, @PathVariable String userId) {
        // @PathVariable_userId 유효성 검증
        if (!userRepository.existsById(userId)) {
            throw new NoSuchElementException("사용자를 찾을 수 없습니다: " + userId);
        }
        try {
            DietCriterion dietCriterion = userService.applyWeight(userId, age, gender);
            return ResponseEntity.ok(dietCriterion);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "사용자 영양소 섭취기준 가중치 조회", security = @SecurityRequirement(name = "BearerAuth"))
    @GetMapping("/{userId}/weight")
    public ResponseEntity<DietCriterionWeightDTO> getDietCriteriaWeight(@PathVariable String userId) {
        // @PathVariable_userId 유효성 검증
        if (!userRepository.existsById(userId)) {
            throw new NoSuchElementException("사용자를 찾을 수 없습니다: " + userId);
        }
        try {
            return ResponseEntity.ok(userService.getDietCriterionWeight(userId));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "사용자 영양소 섭취기준 가중치 조회", security = @SecurityRequirement(name = "BearerAuth"))
    @PostMapping("/{userId}/weight")
    public ResponseEntity<DietCriterionWeightDTO> setDietCriteriaWeight(
            @PathVariable String userId, @RequestBody DietCriterionWeightDTO dietCriterionWeightDTO
    ) {
        // @PathVariable_userId 유효성 검증
        if (!userRepository.existsById(userId)) {
            throw new NoSuchElementException("사용자를 찾을 수 없습니다: " + userId);
        }
        try {
            userService.setDietCriterionWeight(userId, dietCriterionWeightDTO);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
