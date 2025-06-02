package kr.ac.dankook.ace.healthy_meal_backend.service;

import kr.ac.dankook.ace.healthy_meal_backend.repository.MealInfoRepository;
import kr.ac.dankook.ace.healthy_meal_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;

import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MealInfoFoodAnalyzeService {
    @Value("${openai.api.key}")
    private String openAiApiKey;
    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";
    private static final String MODEL = "o4-mini";
    private final UserRepository userRepository;
    private final MealInfoRepository mealInfoRepository;
    private final RestClient restClient;
    private final StorageService storageService;

    @Autowired
    public MealInfoFoodAnalyzeService(
            UserRepository userRepository,
            MealInfoRepository mealInfoRepository,
            StorageService storageService
    ) {
        this.userRepository = userRepository;
        this.mealInfoRepository = mealInfoRepository;
        this.restClient = RestClient.builder()
                .baseUrl(OPENAI_API_URL)
                .defaultHeader("Content-Type", "application/json")
                .requestFactory(createSimpleRequestFactory())
                .build();
        this.storageService = storageService;
    }

    private ClientHttpRequestFactory createSimpleRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(30));  // 연결 타임아웃: 30초
        factory.setReadTimeout(Duration.ofMinutes(5));      // 읽기 타임아웃: 5분
        return factory;
    }

    public List<String> analyzeImage(String fileName, List<String> categories) {
        // 리스트를 문자열로 변환
        String categoriesString = String.join(", ", categories);

        String base64Img = storageService.convertImageToBase64(fileName);

        // 프롬프트 생성
        String prompt = String.format(
                """
                        이미지를 보고 다음 카테고리 목록 중에서 이미지에 있는 모든 카테고리를 찾아주세요.
                        
                        음식 목록: %s
                        
                        규칙:
                        1. 반드시 제공된 카테고리 목록에서만 선택해주세요
                        2. 이미지에 보이는 각 음식당 가장 적합한 카테고리를 찾아주세요
                        3. 음식이 여러 개가 있다면 쉼표(,)로 구분해서 나열해주세요
                        4. 각 카테고리 이름은 목록에 있는 정확한 카테고리를 사용해주세요
                        5. 만약 목록에 해당하는 음식이 없다면 '해당없음'이라고 답해주세요
                        6. 답변 예시: '김치찌개, 밥, 계란후라이' 또는 '피자' 또는 '해당없음'
                        7. 추가 설명 없이 카테고리 이름만 작성해주세요""",
                categoriesString
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
                                                        "url", "data:image/jpeg;base64," + base64Img,
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

            // 결과를 리스트로 변환하고 검증
            return parseAndValidateResult(result, categories);

        } catch (Exception e) {
            throw new RuntimeException("이미지 분석 중 오류가 발생했습니다: " + e.getMessage(), e);
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

    private List<String> parseAndValidateResult(String aiResponse, List<String> categories) {
        String trimmedResponse = aiResponse.trim();

        // "해당없음"인 경우
        if ("해당없음".equals(trimmedResponse)) {
            return List.of("해당없음");
        }

        // 쉼표로 분리하여 개별 음식 이름 추출
        List<String> detectedFoods = Arrays.stream(trimmedResponse.split(","))
                .map(String::trim)
                .filter(food -> !food.isEmpty())
                .toList();

        // 각 음식이 실제 목록에 있는지 검증
        List<String> validFoods = new ArrayList<>();

        for (String detectedFood : detectedFoods) {
            if (categories.contains(detectedFood)) {
                validFoods.add(detectedFood);
            } else {
                // 유사한 음식 찾기
                String closestMatch = findClosestMatch(detectedFood, categories);
                if (!"해당없음".equals(closestMatch) && !validFoods.contains(closestMatch)) {
                    validFoods.add(closestMatch);
                }
            }
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
}
