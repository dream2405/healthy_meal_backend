package kr.ac.dankook.ace.healthy_meal_backend.service;

import kr.ac.dankook.ace.healthy_meal_backend.entity.MealInfo;
import kr.ac.dankook.ace.healthy_meal_backend.entity.User;
import kr.ac.dankook.ace.healthy_meal_backend.repository.FoodRepository;
import kr.ac.dankook.ace.healthy_meal_backend.repository.MealInfoRepository;
import kr.ac.dankook.ace.healthy_meal_backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;

import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MealInfoFoodAnalyzeService {
    @Value("${openai.api.key}")
    private String openAiApiKey;
    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";
    private static final String MODEL = "o4-mini";
    private final UserRepository userRepository;
    private final FoodRepository foodRepository;
    private final MealInfoRepository mealInfoRepository;
    private final RestClient restClient;
    private final StorageService storageService;

    private static final Logger logger = LoggerFactory.getLogger(MealInfoFoodAnalyzeService.class);

    @Autowired
    public MealInfoFoodAnalyzeService(
            UserRepository userRepository,
            MealInfoRepository mealInfoRepository,
            StorageService storageService,
            FoodRepository foodRepository
    ) {
        this.userRepository = userRepository;
        this.mealInfoRepository = mealInfoRepository;
        this.restClient = RestClient.builder()
                .baseUrl(OPENAI_API_URL)
                .defaultHeader("Content-Type", "application/json")
                .requestFactory(createSimpleRequestFactory())
                .build();
        this.storageService = storageService;
        this.foodRepository = foodRepository;
    }

    public MealInfo createMealInfo(String imgPath, User user) {
        MealInfo mealInfo = new MealInfo();
        mealInfo.setImgPath(imgPath);
        mealInfo.setUser(user);
        mealInfoRepository.save(mealInfo);
        return mealInfo;
    }

    public List<String> gptAnalyzeImage(String base64Image) {
        long start = System.currentTimeMillis();
        // 프롬프트 생성
        String prompt = String.format(
                """
                        이미지 속에 보이는 음식을 모두 구체적인 이름으로 나열해주세요.
                       
                        ⚠️ 규칙:
                        1. 설명 없이 음식 이름만 쉼표로 구분하여 출력해주세요.
                        2. 만약 식별되는 음식이 없다면 '식별되지않음'이라고 답해주세요
                        
                        출력예시:
                        불고기, 밥, 김치
                        또는
                        식별되지않음"""
        );
        // 요청 body 구성
        Map<String, Object> requestBody = Map.of(
                "model", MODEL,
                "messages", List.of(
                        Map.of(
                                "role", "user",
                                "content", List.of(
                                        Map.of(
                                                "type", "text",
                                                "text", prompt
                                        ),
                                        Map.of(
                                                "type", "image_url",
                                                "image_url", Map.of(
                                                        "url", "data:image/jpeg;base64," + base64Image,
                                                        "detail", "low"
                                                )
                                        )
                                )
                        )
                ),
                "max_completion_tokens", 1000  // 여러 음식 이름을 위해 토큰 증가
        );

        try {
            // OpenAI API 호출
            Map<String, Object> response = restClient.post()
                    .uri("")
                    .header("Authorization", "Bearer " + openAiApiKey)
                    .body(requestBody)
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {});

            // 응답에서 텍스트 추출
            String result = extractContentFromResponse(response);

            long end = System.currentTimeMillis();
            long duration = end - start;
            logger.info("식품목록 GPT분석 소요시간 : {}m{}s / 응답결과 : {}", (duration/1000/60), ((duration/1000)%60), result);

            // 결과를 리스트로 변환하고 검증
            return parseAndValidateResult(result, new ArrayList<>());

        } catch (Exception e) {
            throw new RuntimeException("OpenAI api request 중 오류가 발생했습니다: " + e.getMessage(), e);
        }

    }

    public MealInfo completeMealInfo(MealInfo mealInfo, Float amount, String diary) {
        mealInfo.setIntakeAmount(amount);
        mealInfo.setDiary(diary);
        return mealInfoRepository.save(mealInfo);
    }

    public MealInfo validateMealInfoId(Long mealInfoId, String userId) {
        Optional<MealInfo> mealInfo = mealInfoRepository.findById(mealInfoId);
        if (mealInfo.isEmpty()) {
            throw new NoSuchElementException("분석을 위해 기록된 식단 없음");
        } else if (!mealInfo.get().getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("해당 사용자가 기록한 식단이 아님");
        } else {
            return mealInfo.get();
        }
    }

    public void createFoodMealInfoRelation(Long foodId, Long mealInfoId) {
        var food = foodRepository.findById(foodId);
        var mealInfo = mealInfoRepository.findById(mealInfoId);
        if (food.isEmpty() || mealInfo.isEmpty()) {
            throw new NoSuchElementException("분석된 식품과 기록된 식단 없음");
        } else {
            food.get().addMealInfo(mealInfo.get());
        }
    }

    private String extractContentFromResponse(Map<String, Object> response) {
        try {
            List<Map<String, Object>> choices =
                    (List<Map<String, Object>>) response.get("choices");

            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> firstChoice = choices.get(0);
                Map<String, Object> message =
                        (Map<String, Object>) firstChoice.get("message");

                if (message != null) {
                    return (String) message.get("content");
                }
            }

            return "해당없음";
        } catch (Exception e) {
            return "해당없음";
        }
    }

    private List<String> parseAndValidateResult(String gptResponse, List<String> categories) {
        List<String> validFoods = new ArrayList<>();
        String trimmedResponse = gptResponse.trim();

        // "해당없음"인 경우
        if ("해당없음".equals(trimmedResponse)) {
            return List.of("해당없음");
        }

        // 쉼표로 분리하여 개별 음식 이름 추출
        List<String> detectedFoods = Arrays.stream(trimmedResponse.split(","))
                .map(String::trim)
                .filter(food -> !food.isEmpty())
                .toList();

        // 각 음식이 실제 목록에 있는지 검증 (목록이 notnull일때만)
        if (!categories.isEmpty()) {
            for (String detectedFood : detectedFoods) {
                if (categories.contains(detectedFood)) {
                    validFoods.add(detectedFood);
                } else {
                    // 유사한 음식 찾기
                    String closestMatch = findClosestMatch(detectedFood, categories);
                    if (!"해당하는 레코드 없음".equals(closestMatch) && !validFoods.contains(closestMatch)) {
                        validFoods.add(closestMatch);
                    }
                }
            }
        } else {
            validFoods.addAll(detectedFoods);
        }

        // 중복 제거
        validFoods = validFoods.stream().distinct().collect(Collectors.toList());

        return validFoods.isEmpty() ? List.of("해당없음") : validFoods;
    }

    private String findClosestMatch(String aiResponse, List<String> categories) {
        String lowerResponse = aiResponse.toLowerCase();

        for (String food : categories) {
            if (lowerResponse.contains(food.toLowerCase()) ||
                    food.toLowerCase().contains(lowerResponse)) {
                return food;
            }
        }

        return "해당없음";
    }

    private ClientHttpRequestFactory createSimpleRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(30));  // 연결 타임아웃: 30초
        factory.setReadTimeout(Duration.ofMinutes(5));      // 읽기 타임아웃: 5분
        return factory;
    }
}
