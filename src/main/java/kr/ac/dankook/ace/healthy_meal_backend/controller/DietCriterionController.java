package kr.ac.dankook.ace.healthy_meal_backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.ac.dankook.ace.healthy_meal_backend.service.UserService;
import kr.ac.dankook.ace.healthy_meal_backend.dto.DietCriterionWeightDTO;
import kr.ac.dankook.ace.healthy_meal_backend.entity.DietCriterion;
import kr.ac.dankook.ace.healthy_meal_backend.entity.User;
import kr.ac.dankook.ace.healthy_meal_backend.repository.DietCriterionRepository;
import kr.ac.dankook.ace.healthy_meal_backend.security.CustomUserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/diet-criteria")
@Tag(name = "식단기준")
public class DietCriterionController {

    private final UserService userService;
    private final DietCriterionRepository dietCriterionRepository;

    public DietCriterionController(
            DietCriterionRepository dietCriterionRepository,
            UserService userService
    ) {
        this.dietCriterionRepository = dietCriterionRepository;
        this.userService = userService;
    }

    @Operation(summary = "사용자별 영양소 섭취기준 조회", security = @SecurityRequirement(name = "BearerAuth"))
    @GetMapping("/{userId}")
    public ResponseEntity<DietCriterion> getDietCriteria(
            @RequestParam Integer age, @RequestParam char gender,
            @PathVariable String userId, @AuthenticationPrincipal CustomUserDetails userDetails) {
        // @PathVariable_userId 유효성 검증
        if (!userId.equals(userDetails.getUsername())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
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
    public ResponseEntity<DietCriterionWeightDTO> getDietCriteriaWeight(
            @PathVariable String userId, @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        // @PathVariable_userId 유효성 검증
        if (!userId.equals(userDetails.getUsername())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
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
            @PathVariable String userId, @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody DietCriterionWeightDTO dietCriterionWeightDTO
    ) {
        // @PathVariable_userId 유효성 검증
        if (!userId.equals(userDetails.getUsername())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        try {
            userService.setDietCriterionWeight(userId, dietCriterionWeightDTO);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
