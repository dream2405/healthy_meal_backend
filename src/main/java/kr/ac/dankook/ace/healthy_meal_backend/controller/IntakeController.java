package kr.ac.dankook.ace.healthy_meal_backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.ac.dankook.ace.healthy_meal_backend.dto.*;
import kr.ac.dankook.ace.healthy_meal_backend.entity.DailyIntake;
import kr.ac.dankook.ace.healthy_meal_backend.repository.*;
import kr.ac.dankook.ace.healthy_meal_backend.security.CustomUserDetails;
import kr.ac.dankook.ace.healthy_meal_backend.service.MealRecordService;
import kr.ac.dankook.ace.healthy_meal_backend.service.NutrientIntakeService;
import kr.ac.dankook.ace.healthy_meal_backend.service.NutritionService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users/{userId}")
@Tag(name = "영양소 섭취분석")
public class IntakeController {
    private final DailyIntakeRepository dailyIntakeRepository;
    private final NutritionService nutritionService;
    private final NutrientIntakeService nutrientIntakeService;

    @GetMapping("/daily-intake")
    @Operation(summary = "사용자 영양소 섭취현황 조회", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<NutrientValuesDTO> getDailyIntake(
            @PathVariable String userId,
            @RequestParam(value = "date", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        String authenticatedUserId = userDetails.getUsername();
        if (!authenticatedUserId.equals(userId)) {
            throw new AccessDeniedException("해당 사용자에 대한 접근 권한이 없습니다");
        }
        try {
            List<NutrientValueElement> nutrientValueElements = nutrientIntakeService.getDailyIntake(userId, date);
            return ResponseEntity.ok().body(new NutrientValuesDTO(nutrientValueElements));
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/daily-intakes")
    @Operation(summary = "사용자 영양소 섭취현황 기간단위 조회", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<DailyIntakesDTO> getDailyIntakes(
            @PathVariable String userId,
            @RequestParam(value = "start_date", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(value = "end_date", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        String authenticatedUserId = userDetails.getUsername();
        if (!authenticatedUserId.equals(userId)) {
            throw new AccessDeniedException("해당 사용자에 대한 접근 권한이 없습니다");
        }
        return ResponseEntity.ok().body(new DailyIntakesDTO(nutrientIntakeService.getDailyIntakes(userId, startDate, endDate)));
    }
    private List<LocalDate> getDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null && endDate == null) {
            return Collections.emptyList();
        }
        if (startDate == null) {
            return List.of(endDate);
        }
        if (endDate == null) {
            return List.of(startDate);
        }
        return startDate.datesUntil(endDate.plusDays(1)).collect(Collectors.toList());
    }

    @GetMapping("/daily-intake/score")
    @Operation(summary = "사용자 영양소 점수 조회", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<NutrientValuesDTO> getDailyIntakeScore(
            @PathVariable String userId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        String authenticatedUserId = userDetails.getUsername();
        if (!authenticatedUserId.equals(userId)) {
            throw new AccessDeniedException("해당 사용자에 대한 접근 권한이 없습니다");
        }
        return ResponseEntity.ok().body(nutrientIntakeService.calcDailyIntakeScore(userId, nutritionService.getDietCriterion(userId).getNutrientValues()));
    }

    @GetMapping("/daily-intake/scores")
    @Operation(summary = "사용자 영양소 점수 기간단위 조회", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<DailyScoresDTO> getDailyIntakeScores(
            @PathVariable String userId,
            @RequestParam(value = "start_date", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(value = "end_date", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        String authenticatedUserId = userDetails.getUsername();
        if (!authenticatedUserId.equals(userId)) {
            throw new AccessDeniedException("해당 사용자에 대한 접근 권한이 없습니다");
        }
        List<DailyScoreElement> dailyScoreElements = new ArrayList<>();
        for(LocalDate date : getDateRange(startDate, endDate)) {
            DailyScoreElement dailyScoreElement = new DailyScoreElement();
            dailyScoreElement.setDate(date);
            Integer score = dailyIntakeRepository.findByUserIdAndDay(userId, date)
                    .map(DailyIntake::getDailyscore) // DailyIntake 객체가 있다면 getDailyscore 호출
                    .orElse(0);                      // 없다면 0 리턴

            dailyScoreElement.setDailyscore(score);
            dailyScoreElements.add(dailyScoreElement);
        }
        return ResponseEntity.ok().body(new DailyScoresDTO(dailyScoreElements));
    }
}