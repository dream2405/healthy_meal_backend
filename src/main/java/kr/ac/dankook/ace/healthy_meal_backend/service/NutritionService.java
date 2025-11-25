package kr.ac.dankook.ace.healthy_meal_backend.service;

import jakarta.transaction.Transactional;
import kr.ac.dankook.ace.healthy_meal_backend.dto.NutrientValueElement;
import kr.ac.dankook.ace.healthy_meal_backend.dto.NutrientValuesDTO;
import kr.ac.dankook.ace.healthy_meal_backend.entity.DietCriterion;
import kr.ac.dankook.ace.healthy_meal_backend.entity.NutrientWeight;
import kr.ac.dankook.ace.healthy_meal_backend.entity.User;
import kr.ac.dankook.ace.healthy_meal_backend.repository.DietCriterionRepository;
import kr.ac.dankook.ace.healthy_meal_backend.repository.NutrientWeightRepository;
import kr.ac.dankook.ace.healthy_meal_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.*;

@Service
public class NutritionService {
    private final UserRepository userRepository;
    private final DietCriterionRepository dietCriterionRepository;
    private final NutrientWeightRepository nutrientWeightRepository;
    @Autowired
    public NutritionService(
            UserRepository userRepository,
            DietCriterionRepository dietCriterionRepository,
            NutrientWeightRepository nutrientWeightRepository
    ) {
        this.userRepository = userRepository;
        this.dietCriterionRepository = dietCriterionRepository;
        this.nutrientWeightRepository = nutrientWeightRepository;
    }

    public NutrientValuesDTO getDietCriterion(String userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NoSuchElementException("해당하는 사용자가 없음"));
        DietCriterion dietCriterion = dietCriterionRepository.findApplicableCriterion(user.getAge(), user.getGender()).orElseThrow(() -> new NoSuchElementException("사용자에 해당하는 영양섭취 기준이 없음"));
        List<NutrientWeight> nutrientWeights = nutrientWeightRepository.findByUserId(userId);

        NutrientValuesDTO dto = new NutrientValuesDTO();
        for(NutrientWeight nutrientWeight : nutrientWeights) {
            NutrientValueElement nutrientValueElement = new NutrientValueElement();
            String nutrientName = nutrientWeight.getNutrientName();
            nutrientValueElement.setNutrientName(nutrientName);
            nutrientValueElement.setValue(getFieldValue(dietCriterion, nutrientName) * nutrientWeight.getWeight());
            dto.getNutrientValues().add(nutrientValueElement);
        }
        return dto;
    }
    private Double getFieldValue(DietCriterion criterion, String nutrientName) {
        try {
            Field field = DietCriterion.class.getDeclaredField(nutrientName);
            field.setAccessible(true);
            return (Double) field.get(criterion);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("사용자의 영양소 가중치 데이터에 문제가 있습니다: " + nutrientName);
        }
    }

    public List<NutrientValueElement> getNutrientWeights(String userId) {
        List<NutrientWeight> nutrientWeights = nutrientWeightRepository.findByUserId(userId);
        List<NutrientValueElement> nutrientValueElements = new ArrayList<>();

        for (NutrientWeight nutrientWeight: nutrientWeights) {
            NutrientValueElement nutrientValueElement = new NutrientValueElement();
            nutrientValueElement.setNutrientName(nutrientWeight.getNutrientName());
            nutrientValueElement.setValue(nutrientWeight.getWeight());
            nutrientValueElements.add(nutrientValueElement);
        }
        return nutrientValueElements;
    }

    @Transactional
    public void setNutrientWeights(String userId, NutrientValuesDTO nutrientValuesDTO) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NoSuchElementException("해당하는 사용자가 없음"));
        nutrientWeightRepository.deleteByUserId(userId);
        for(NutrientValueElement nutrientValueElement: nutrientValuesDTO.getNutrientValues()) {
            NutrientWeight nutrientWeight = new NutrientWeight();
            nutrientWeight.setNutrientName(nutrientValueElement.getNutrientName());
            nutrientWeight.setWeight(nutrientValueElement.getValue());
            nutrientWeight.setUser(user);
            nutrientWeightRepository.save(nutrientWeight);
        }
    }

    @Transactional
    public void patchNutrientWeights(String userId, NutrientValuesDTO nutrientValuesDTO) {
        for(NutrientValueElement nutrientValueElement: nutrientValuesDTO.getNutrientValues()) {
            NutrientWeight nutrientWeight = nutrientWeightRepository.findByUserIdAndNutrientName(userId, nutrientValueElement.getNutrientName()).orElseThrow(() -> new NoSuchElementException("해당하는 사용자의 영양소 목록이 없음"));
            nutrientWeight.setWeight(nutrientValueElement.getValue());
            nutrientWeightRepository.save(nutrientWeight);
        }
    }
}
