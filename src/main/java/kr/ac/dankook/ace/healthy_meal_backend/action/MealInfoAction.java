package kr.ac.dankook.ace.healthy_meal_backend.action;

import kr.ac.dankook.ace.healthy_meal_backend.entity.DailyIntake;
import kr.ac.dankook.ace.healthy_meal_backend.entity.MealInfo;
import kr.ac.dankook.ace.healthy_meal_backend.entity.User;
import kr.ac.dankook.ace.healthy_meal_backend.service.MealInfoFoodAnalyzeService;
import kr.ac.dankook.ace.healthy_meal_backend.service.NutrientIntakeService;
import kr.ac.dankook.ace.healthy_meal_backend.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class MealInfoAction {

    private final MealInfoFoodAnalyzeService mealInfoFoodAnalyzeService;
    private final NutrientIntakeService nutrientIntakeService;
    private final StorageService storageService;

    @Autowired
    MealInfoAction(
            MealInfoFoodAnalyzeService mealInfoFoodAnalyzeService,
            StorageService storageService,
            NutrientIntakeService nutrientIntakeservice
    ) {
        this.mealInfoFoodAnalyzeService = mealInfoFoodAnalyzeService;
        this.storageService = storageService;
        this.nutrientIntakeService = nutrientIntakeservice;
    }

    public MealInfo createMealInfo(MultipartFile file, User user) {
        String filePath = storageService.store(file);
        return mealInfoFoodAnalyzeService.createMealInfo(filePath, user);
    }

    public List<String> analyzeMealInfo(Long mealInfoId, String userId) {
        MealInfo mealInfo = mealInfoFoodAnalyzeService.validateMealInfoId(mealInfoId, userId);
        String base64Image = storageService.convertImageToBase64(mealInfo.getImgPath());
        List<String> gptResponse = mealInfoFoodAnalyzeService.gptAnalyzeImage(base64Image);
        List<String> analyzedRepresentativeFoods = mealInfoFoodAnalyzeService.representativeFoodRecordMapper(gptResponse);
        return mealInfoFoodAnalyzeService.foodRecordMapper(analyzedRepresentativeFoods, gptResponse);
        //mealInfoFoodAnalyzeService.createFoodMealInfoRelation(null, null); -> 분석된 food 개수만큼 불러야함
    }

    public MealInfo completeMealInfo(User user, Long mealInfoId, Float amount, String diary, List<String> confirmedFoods) {
        MealInfo mealInfo = mealInfoFoodAnalyzeService.validateMealInfoId(mealInfoId, user.getId());
        MealInfo updatedMealInfo = mealInfoFoodAnalyzeService.completeMealInfo(mealInfo, amount, diary);
        for (String food : confirmedFoods) {
            mealInfoFoodAnalyzeService.createFoodMealInfoRelation(food, mealInfoId);
        }
        DailyIntake dailyIntake = nutrientIntakeService.updateDailyIntake(updatedMealInfo, user, updatedMealInfo.getCreatedAt().toLocalDate());
        return updatedMealInfo;
    }
}
