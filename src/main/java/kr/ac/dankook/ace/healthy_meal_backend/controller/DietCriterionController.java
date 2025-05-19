package kr.ac.dankook.ace.healthy_meal_backend.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import kr.ac.dankook.ace.healthy_meal_backend.repository.DietCriterionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/diet-criteria")
@Tag(name = "식단기준")
public class DietCriterionController {
    private final DietCriterionRepository dietCriterionRepository;

    @Autowired
    public DietCriterionController(DietCriterionRepository dietCriterionRepository) {
        this.dietCriterionRepository = dietCriterionRepository;
    }

    
}
