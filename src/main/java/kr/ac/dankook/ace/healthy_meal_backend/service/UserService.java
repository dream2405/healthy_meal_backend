package kr.ac.dankook.ace.healthy_meal_backend.service;

import jakarta.transaction.Transactional;
import kr.ac.dankook.ace.healthy_meal_backend.dto.DietCriterionWeightDTO;
import kr.ac.dankook.ace.healthy_meal_backend.entity.DietCriterion;
import kr.ac.dankook.ace.healthy_meal_backend.entity.User;
import kr.ac.dankook.ace.healthy_meal_backend.repository.DietCriterionRepository;
import kr.ac.dankook.ace.healthy_meal_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final DietCriterionRepository dietCriterionRepository;

    @Autowired
    public UserService(
            UserRepository userRepository,
            DietCriterionRepository dietCriterionRepository
    ) {
        this.userRepository = userRepository;
        this.dietCriterionRepository = dietCriterionRepository;
    }

    public DietCriterionWeightDTO getDietCriterionWeight(String userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            throw new NoSuchElementException("해당하는 사용자가 없음");
        }
        List<String> weights = Arrays.asList((user.get().getCritweight()).split("\\s+"));
        return new DietCriterionWeightDTO(
                Integer.parseInt(weights.get(0)),       //private Integer energyKcalWeight;
                Integer.parseInt(weights.get(1)),       //private Integer carbohydrateGWeight;
                Integer.parseInt(weights.get(2)),       //private Integer fatGWeight;
                Integer.parseInt(weights.get(3)),       //private Integer proteinGWeight;
                Integer.parseInt(weights.get(4)),       //private Integer celluloseGWeight;
                Integer.parseInt(weights.get(5)),       //private Integer sugarsGWeight;
                Integer.parseInt(weights.get(6)),       //private Integer sodiumMgWeight;
                Integer.parseInt(weights.get(7))        //private Integer cholesterolMgWeight;
        );
    }

    @Transactional
    public void setDietCriterionWeight(String userId, DietCriterionWeightDTO dietCriterionWeightDTO) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NoSuchElementException("해당하는 사용자가 없음"));
        String critWeight = dietCriterionWeightDTO
                .toList()
                .stream()
                .map(String::valueOf)
                .collect(Collectors.joining(" "));
        user.updateCritweight(critWeight);
    }

    public DietCriterion applyWeight(String userId, Integer age, char gender) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NoSuchElementException("해당하는 사용자가 없음"));
        DietCriterion dietCriterion = dietCriterionRepository.findApplicableCriterion(age, gender).orElseThrow(() -> new NoSuchElementException(("해당하는 영양섭취 기준이 없음")));
        List<String> weights = Arrays.asList((user.getCritweight()).split("\\s+"));
        List<Float> floatWeights = weights.stream().map(Float::parseFloat).toList();
        //System.out.println("칼로리계산량 : " + dietCriterion.getEnergyKcal() + " / 가중치계산량 : " + (floatWeights.get(0).intValue()/100));
        dietCriterion.setEnergyKcal(dietCriterion.getEnergyKcal()*(floatWeights.get(0).intValue()/100f));
        dietCriterion.setCarbohydrateG(dietCriterion.getCarbohydrateG()*(floatWeights.get(1).intValue()/100f));
        dietCriterion.setFatG(dietCriterion.getFatG()*(floatWeights.get(2).intValue()/100f));
        dietCriterion.setProteinG(dietCriterion.getProteinG()*(floatWeights.get(3).intValue()/100f));
        dietCriterion.setCelluloseG(dietCriterion.getCelluloseG()*(floatWeights.get(4).intValue()/100f));
        dietCriterion.setSugarsG(dietCriterion.getSugarsG()*(floatWeights.get(5).intValue()/100f));
        dietCriterion.setSodiumMg(dietCriterion.getSodiumMg()*(floatWeights.get(6).intValue()/100f));
        dietCriterion.setCholesterolMg(dietCriterion.getCholesterolMg()*(floatWeights.get(7).intValue()/100f));
        return dietCriterion;
    }
}
