package kr.ac.dankook.ace.healthy_meal_backend.action;

import kr.ac.dankook.ace.healthy_meal_backend.entity.MealInfo;
import kr.ac.dankook.ace.healthy_meal_backend.entity.User;
import kr.ac.dankook.ace.healthy_meal_backend.service.MealInfoFoodAnalyzeService;
import kr.ac.dankook.ace.healthy_meal_backend.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class MealAction {

    private final MealInfoFoodAnalyzeService mealInfoFoodAnalyzeService;
    private final StorageService storageService;

    @Autowired
    MealAction(
            MealInfoFoodAnalyzeService mealInfoFoodAnalyzeService,
            StorageService storageService
    ) {
        this.mealInfoFoodAnalyzeService = mealInfoFoodAnalyzeService;
        this.storageService = storageService;
    }

    public MealInfo createMealInfo(MultipartFile file, User user) {
        String filePath = storageService.store(file);
        return mealInfoFoodAnalyzeService.createMealInfo(filePath, user);
    }

    public List<String> analyzeMealInfo(Long mealInfoId) {
        MealInfo mealInfo = mealInfoFoodAnalyzeService.validateMealInfoId(mealInfoId);
        String base64Image = storageService.convertImageToBase64(mealInfo.getImgPath());
        List<String> gptResponse = mealInfoFoodAnalyzeService.gptAnalyzeImage(base64Image);

        return gptResponse;
    }
}
