package kr.ac.dankook.ace.healthy_meal_backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.ac.dankook.ace.healthy_meal_backend.dto.AnalyzeResponseDTO;
import kr.ac.dankook.ace.healthy_meal_backend.dto.MealRecordDTO;
import kr.ac.dankook.ace.healthy_meal_backend.dto.MealRecordRequestDTO;
import kr.ac.dankook.ace.healthy_meal_backend.entity.MealRecord;
import kr.ac.dankook.ace.healthy_meal_backend.repository.DailyIntakeRepository;
import kr.ac.dankook.ace.healthy_meal_backend.repository.FoodRepository;
import kr.ac.dankook.ace.healthy_meal_backend.repository.MealRecordRepository;
import kr.ac.dankook.ace.healthy_meal_backend.repository.UserRepository;
import kr.ac.dankook.ace.healthy_meal_backend.security.CustomUserDetails;
import kr.ac.dankook.ace.healthy_meal_backend.service.MealRecordService;
import kr.ac.dankook.ace.healthy_meal_backend.service.NutrientIntakeService;
import kr.ac.dankook.ace.healthy_meal_backend.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users/{userId}")
@Tag(name = "유저")
public class MealRecordController {

    private final NutrientIntakeService nutrientIntakeService;
    private final MealRecordService mealRecordService;
    private final StorageService storageService;

    private static final Logger logger = LoggerFactory.getLogger(MealRecordController.class);

    @GetMapping("/mealrecords")
    @Operation(summary = "해당 날짜의 식단기록정보 가져오기", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<List<MealRecordDTO>> getMealInfo(
            @PathVariable String userId,
            @RequestParam(value = "date", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        String authenticatedUserId = userDetails.getUsername();
        if (!authenticatedUserId.equals(userId)) {
            throw new AccessDeniedException("해당 사용자에 대한 접근 권한이 없습니다 (잘못된 userId)");
        }
        return ResponseEntity.ok().body(mealRecordService.getMealRecord(userId, date));
    }


    @PostMapping(value = "/mealrecord")
    @Operation(summary = "주어진 정보로 사용자의 식단정보 기록", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<Object> createMealRecord(
            @PathVariable String userId,
            @RequestBody MealRecordRequestDTO mealRecordRequestDTO,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        String authenticatedUserId = userDetails.getUsername();
        if (!authenticatedUserId.equals(userId)) {
            throw new AccessDeniedException("해당 사용자에 대한 접근 권한이 없습니다 (잘못된 userId)");
        }
        if (storageService.existsInTemp(mealRecordRequestDTO.getImgPath())) {
            storageService.storeRoot(mealRecordRequestDTO.getImgPath());
        } else {
            return ResponseEntity.badRequest().body("유효하지 않은 식단기록 요청 (이미지 분석 및 저장 필요)");
        }
        MealRecord mealRecord = mealRecordService.createMealRecord(userId, mealRecordRequestDTO);
        nutrientIntakeService.applyDailyIntake(mealRecord);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PatchMapping(value = "/mealrecord")
    @Operation(summary = "주어진 정보로 사용자의 식단정보 수정 (아직불완전하니까쓰지마세요)", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<Object> deleteMealRecord(
            @PathVariable String userId,
            @RequestParam Long mealRecordId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        String authenticatedUserId = userDetails.getUsername();
        if (!authenticatedUserId.equals(userId)) {
            throw new AccessDeniedException("해당 사용자에 대한 접근 권한이 없습니다 (잘못된 userId)");
        }
        mealRecordService.deleteMealRecord(userId, mealRecordId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping(value = "/mealrecord")
    @Operation(summary = "주어진 정보로 사용자의 식단정보 수정 (아직불완전하니까쓰지마세요)", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<Object> patchMealRecord(
            @PathVariable String userId,
            @RequestBody MealRecordRequestDTO mealRecordDTO,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        String authenticatedUserId = userDetails.getUsername();
        if (!authenticatedUserId.equals(userId)) {
            throw new AccessDeniedException("해당 사용자에 대한 접근 권한이 없습니다 (잘못된 userId)");
        }
        MealRecord mealRecord = mealRecordService.patchMealRecord(userId, mealRecordDTO);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}