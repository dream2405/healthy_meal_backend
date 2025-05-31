package kr.ac.dankook.ace.healthy_meal_backend.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import kr.ac.dankook.ace.healthy_meal_backend.entity.DietCriterion;
import kr.ac.dankook.ace.healthy_meal_backend.repository.DietCriterionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/diet-criteria")
@Tag(name = "식단기준")
public class DietCriterionController {
    private final DietCriterionRepository dietCriterionRepository;

    @Autowired
    public DietCriterionController(DietCriterionRepository dietCriterionRepository) {
        this.dietCriterionRepository = dietCriterionRepository;
    }

    @GetMapping("/")
    public ResponseEntity<DietCriterion> getDietCriteria(@RequestParam Integer age, @RequestParam char gender) {
        Optional<DietCriterion> dietCriterion = dietCriterionRepository.findApplicableCriterion(age, gender);
        return dietCriterion.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
