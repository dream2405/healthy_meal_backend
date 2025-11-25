package kr.ac.dankook.ace.healthy_meal_backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.ac.dankook.ace.healthy_meal_backend.dto.NutrientModelDTO;
import kr.ac.dankook.ace.healthy_meal_backend.dto.NutrientValuesDTO;
import kr.ac.dankook.ace.healthy_meal_backend.dto.ResultMessageResponseDTO;
import kr.ac.dankook.ace.healthy_meal_backend.entity.User;
import kr.ac.dankook.ace.healthy_meal_backend.repository.UserRepository;
import kr.ac.dankook.ace.healthy_meal_backend.security.CustomUserDetails;
import kr.ac.dankook.ace.healthy_meal_backend.service.NutritionService;
import kr.ac.dankook.ace.healthy_meal_backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

@RestController
@RequiredArgsConstructor // 의존성주입
@RequestMapping("/users/{userId}")
@Tag(name = "영양소 섭취모델")
public class NutritionController {
    private final UserRepository userRepository;
    private final NutritionService nutritionService;

    @Operation(summary = "사용자 영양소 섭취기준 조회", security = @SecurityRequirement(name = "BearerAuth"))
    @GetMapping("/diet-criterion")
    public ResponseEntity<NutrientValuesDTO> getDietCriterion(
            @PathVariable String userId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String authenticatedUserId = userDetails.getUsername();
        if (!authenticatedUserId.equals(userId)) {
            throw new AccessDeniedException("해당 사용자에 대한 권한이 없습니다 (잘못된 userId)");
        }
        return ResponseEntity.ok(nutritionService.getDietCriterion(userId));
    }

    @Operation(summary = "사용자 식생활 목표모델 조회", security = @SecurityRequirement(name = "BearerAuth"))
    @GetMapping("/nutrient-model")
    public ResponseEntity<NutrientModelDTO> getNutrientModel(
            @PathVariable String userId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String authenticatedUserId = userDetails.getUsername();
        if (!authenticatedUserId.equals(userId)) {
            throw new AccessDeniedException("해당 사용자에 대한 접근 권한이 없습니다 (잘못된 userId)");
        }
        User user = userRepository.findById(userId).orElseThrow(() -> new NoSuchElementException("해당하는 사용자가 없음"));
        NutrientModelDTO nutrientModelDTO = new NutrientModelDTO();
        nutrientModelDTO.setNutrientLevel(user.getNutritionLevel());
        nutrientModelDTO.setModelname(user.getNutrientModelname());
        return ResponseEntity.ok(nutrientModelDTO);
    }

    @Operation(summary = "사용자 식생활 목표모델 변경", security = @SecurityRequirement(name = "BearerAuth"))
    @PatchMapping("/nutrient-model")
    public ResponseEntity<ResultMessageResponseDTO> patchNutrientModel(
            @PathVariable String userId,
            @RequestBody NutrientModelDTO nutrientModelDTO,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String authenticatedUserId = userDetails.getUsername();
        if (!authenticatedUserId.equals(userId)) {
            throw new AccessDeniedException("해당 사용자에 대한 접근 권한이 없습니다");
        }
        try {
            User user = userRepository.findById(userId).orElseThrow(() -> new NoSuchElementException("해당하는 사용자가 없음"));
            user.setNutrientModelname(nutrientModelDTO.getModelname());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ResultMessageResponseDTO("식생활 목표모델 변경에 실패하였습니다 : " + e.getMessage()));
        }
        return ResponseEntity.ok(new ResultMessageResponseDTO("식생활 목표모델 변경을 완료하였습니다."));
    }

    @Operation(summary = "사용자 영양소 섭취기준 가중치 조회", security = @SecurityRequirement(name = "BearerAuth"))
    @GetMapping("/nutrient-weights")
    public ResponseEntity<NutrientValuesDTO> getNutrientWeight(
            @PathVariable String userId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String authenticatedUserId = userDetails.getUsername();
        if (!authenticatedUserId.equals(userId)) {
            throw new AccessDeniedException("해당 사용자에 대한 접근 권한이 없습니다 (잘못된 userId)");
        }
        NutrientValuesDTO nutrientValuesDTO = new NutrientValuesDTO();
        nutrientValuesDTO.setNutrientValues(nutritionService.getNutrientWeights(userId));
        return ResponseEntity.ok(nutrientValuesDTO);
    }

    @Operation(summary = "사용자 영양소 섭취기준 가중치 생성", security = @SecurityRequirement(name = "BearerAuth"))
    @PostMapping("/nutrient-weights")
    public ResponseEntity<ResultMessageResponseDTO> postDietCriteriaWeight(
            @PathVariable String userId,
            @RequestBody NutrientValuesDTO nutrientValuesDTO,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        String authenticatedUserId = userDetails.getUsername();
        if (!authenticatedUserId.equals(userId)) {
            throw new AccessDeniedException("해당 사용자에 대한 접근 권한이 없습니다 (잘못된 userId)");
        }
        try {
            nutritionService.setNutrientWeights(userId, nutrientValuesDTO);
            return ResponseEntity.ok(new ResultMessageResponseDTO("사용자의 영양소 목록 및 가중치 생성을 완료하였습니다."));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "사용자 영양소 섭취기준 가중치 목록변경", security = @SecurityRequirement(name = "BearerAuth"))
    @PutMapping("/nutrient-weights")
    public ResponseEntity<ResultMessageResponseDTO> putDietCriteriaWeight(
            @PathVariable String userId,
            @RequestBody NutrientValuesDTO nutrientValuesDTO,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        String authenticatedUserId = userDetails.getUsername();
        if (!authenticatedUserId.equals(userId)) {
            throw new AccessDeniedException("해당 사용자에 대한 접근 권한이 없습니다 (잘못된 userId)");
        }
        try {
            nutritionService.setNutrientWeights(userId, nutrientValuesDTO);
            return ResponseEntity.ok(new ResultMessageResponseDTO("사용자의 영양소 목록 및 가중치 변경을 완료하였습니다."));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "사용자 영양소 섭취기준 가중치 목록변경", security = @SecurityRequirement(name = "BearerAuth"))
    @PatchMapping("/nutrient-weights")
    public ResponseEntity<ResultMessageResponseDTO> patchDietCriteriaWeight(
            @PathVariable String userId,
            @RequestBody NutrientValuesDTO nutrientValuesDTO,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        String authenticatedUserId = userDetails.getUsername();
        if (!authenticatedUserId.equals(userId)) {
            throw new AccessDeniedException("해당 사용자에 대한 접근 권한이 없습니다 (잘못된 userId)");
        }
        try {
            nutritionService.patchNutrientWeights(userId, nutrientValuesDTO);
            return ResponseEntity.ok(new ResultMessageResponseDTO("사용자의 영양소 가중치 변경을 완료하였습니다."));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

}
