package kr.ac.dankook.ace.healthy_meal_backend.service;

import jakarta.transaction.Transactional;
import kr.ac.dankook.ace.healthy_meal_backend.entity.Food;
import kr.ac.dankook.ace.healthy_meal_backend.entity.MealInfo;
import kr.ac.dankook.ace.healthy_meal_backend.entity.User;
import kr.ac.dankook.ace.healthy_meal_backend.repository.FoodRepository;
import kr.ac.dankook.ace.healthy_meal_backend.repository.MealInfoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.apache.commons.text.similarity.LevenshteinDistance;

import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;
import java.util.UUID;

@Service
public class MealInfoFoodAnalyzeService {
    @Value("${openai.api.key}")
    private String openAiApiKey;
    private static final String OPENAI_API_URL = "https://api.openai.com/v1/";
    private static final String OPENAI_CONV = OPENAI_API_URL + "conversations";
    private static final String OPENAI_RESP = OPENAI_API_URL + "conversations/";
    private static final String MODEL = "gpt-4.1-mini";
    private static final float COSINETHRESHOLD = 0.35f;
    private final FoodRepository foodRepository;
    private final MealInfoRepository mealInfoRepository;
    private final RestClient restClient;

    private static final Logger logger = LoggerFactory.getLogger(MealInfoFoodAnalyzeService.class);

    @Autowired
    public MealInfoFoodAnalyzeService(
            MealInfoRepository mealInfoRepository,
            FoodRepository foodRepository
    ) {
        this.mealInfoRepository = mealInfoRepository;
        this.restClient = RestClient.builder()
                //.baseUrl(OPENAI_API_URL) -> URL은 추후 개별적으로 설정하여 HTTP Request
                .defaultHeader("Content-Type", "application/json")
                .requestFactory(createSimpleRequestFactory())
                .build();
        this.foodRepository = foodRepository;
    }

    @Transactional
    public MealInfo createMealInfo(String imgPath, User user) {
        MealInfo mealInfo = new MealInfo();
        mealInfo.setImgPath(imgPath);
        mealInfo.setUser(user);
        mealInfoRepository.save(mealInfo);
        return mealInfo;
    }

    /*
    """
                이미지 속 음식은 한 그릇 단위의 요리라고 가정하고, 그릇 단위로 구체적인 음식명을 출력해주세요.

                ⚠️ 규칙:
                1. 설명 없이 음식 이름만 쉼표로 구분하여 출력.
                2. 만약 식별되는 음식이 없다면 '식별되지않음' 이라고 출력.
                3. 음식 이름은 단어별로 띄어쓰기하여 출력.
                4. 단일 음식에 대해서 쉼표구분 없이 최대한 띄어쓰기를 사용해서 출력. (스파게티, 토마토 크림 소스 -> 토마토 크림 스파게티)
                5. 예외항목: 파스타 -> 스파게티, 돈까스 -> 돈가스
                6. 하나의 음식에는 하나의 음식명만 출력.


                출력예시:
                불고기, 밥, 김치, 초콜릿 칩 스콘
                또는
                식별되지않음""";

     */

    public List<String> gptAnalyzeImage(String base64Image) {
        long start = System.currentTimeMillis();

        String convUUID;
        List<String> majorCategories = foodRepository.findDistinctMajorCategoryNative();
        List<String> majorCategoriesResult;
        List<String> representativeFoods = new ArrayList<>();
        List<String> representativeFoodsResult;
        //List<String> foods = new ArrayList<>();
        List<String> foodResult = new ArrayList<>();
        List<Food> foodList = new ArrayList<>();
        List<String> result = new ArrayList<>();

        // GPT Conversation 방파기 - conversation ID 반환받기 -> createAnalyze()
        convUUID = createAnalyze();
        // 첫번째 gpt 분석 - 식단 이미지로 맞는 대분류 매칭 -> firstanalyzeImage()
        majorCategoriesResult = firstanalyzeImage(base64Image, majorCategories, convUUID);
        for (String majorCategory : majorCategoriesResult) {
            representativeFoods.addAll(foodRepository.findDistinctRepresentativeFoodByMajorCategory(majorCategory));
        }
        // 두번째 gpt 분석 - 대표 음식 매칭 - analyzeImage()
        representativeFoodsResult = analyzeImage(representativeFoods, convUUID);
        for (String representativeFood : representativeFoodsResult) {
            List<String> foods = new ArrayList<>();
            foods.addAll(foodRepository.findDistinctNameByRepresentativeFood(representativeFood));

            // 마지막 gpt 분석 - 최종 음식 매칭 - analyzeImage()
            foodResult.addAll(analyzeImage(foods, convUUID));
        }
        // 마지막 gpt 분석 - 최종 음식 매칭 - analyzeImage()
        //foodResult = analyzeImage(foods, convUUID);
        // 최종 데이터베이스 검증 및 결과 반환
        for (String foodName : foodResult) {
            foodList.addAll(foodRepository.findAllByName(foodName));
        }

        long end = System.currentTimeMillis();
        logger.info("GPT 사진분석 소요시간 : {} s", (end - start)/1000);

        if (foodList.isEmpty()) {
            return result;
        } else {
            result = foodList.stream()
                    .map(Food::getName)
                    .collect(Collectors.toList());
            return result;
        }
    }
    private String createAnalyze() {
        // 요청 body 구성
        Map<String, Object> requestBody = Map.of(
                "metadata", Map.of(
                        "topic", "healthymeal"
                ),
                "items", List.of(
                        Map.of(
                                "type", "message",
                                "role", "user",
                                "content", "hello!"
                        )
                )
        );
        try {
            // OpenAI API 호출
            String url = OPENAI_CONV;
            Map<String, Object> response = restClient.post()
                    .uri(url)
                    .header("Authorization", "Bearer " + openAiApiKey)
                    .body(requestBody)
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {
                    });

            // 응답에서 텍스트 추출
            String result = response.get("id").toString();
            System.out.println("생성된대화방 : " + result);

            return result;

        } catch (Exception e) {
            throw new RuntimeException("conv 생성중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }
    private List<String> firstanalyzeImage(String base64Image, List<String> categories, String convUUID) {
        // 리스트를 문자열로 변환
        String categoriesString = String.join(", ", categories);
        System.out.println("GPT 선제시목록(" + categories.size() + ")개 : " + categoriesString);

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
                "items", List.of(
                        Map.of(
                                "type", "message",
                                "role", "user",
                                "content", List.of(
                                        Map.of(
                                                "type", "input_text",
                                                "text", prompt
                                        ),
                                        Map.of(
                                                "type", "input_image",
                                                //"image_url", "data:image/jpeg;base64," + base64Image -> 이거 진짜 어케함......
                                                "image_url", "https://images.unsplash.com/photo-1604382354936-07c5d9983bd3?fm=jpg&q=60&w=3000&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxzZWFyY2h8N3x8cGl6emF8ZW58MHx8MHx8fDA%3D"
                                        )
                                )
                        )
                )
                //"max_output_tokens", 1000  // 여러 음식 이름을 위해 토큰 증가
        );

        try {
            // OpenAI API 호출
            Map<String, Object> response = restClient.post()
                    .uri(OPENAI_RESP+convUUID+"/items")
                    .header("Authorization", "Bearer " + openAiApiKey)
                    .body(requestBody)
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {});

            // 응답에서 텍스트 추출
            String result = extractContentFromResponse(response);
            System.out.println("GPT 응답결과 : " + result);

            // 결과를 리스트로 변환하고 검증
            return parseAndValidateResult(result, categories);

        } catch (Exception e) {
            throw new RuntimeException("이미지 분석 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }
    private List<String> analyzeImage(List<String> categories, String convUUID) {
        // 리스트를 문자열로 변환
        String categoriesString = String.join(", ", categories);
        System.out.println("GPT 선제시목록(" + categories.size() + ")개 : " + categoriesString);

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
                        6. 답변 예시: '김치찌개, 밥, 계란후라이' 또는 '피자' 또는 '해당없음''
                        7. 추가 설명 없이 카테고리 이름만 작성해주세요""",
                categoriesString
        );

        // 요청 body 구성
        Map<String, Object> requestBody = Map.of(
                "items", List.of(
                        Map.of(
                                "type", "message",
                                "role", "user",
                                "content", List.of(
                                        Map.of(
                                                "type", "input_text",
                                                "text", prompt
                                        )
                                )
                        )
                )
                //"max_output_tokens", 1000  // 여러 음식 이름을 위해 토큰 증가
        );

        try {
            // OpenAI API 호출
            Map<String, Object> response = restClient.post()
                    .uri(OPENAI_RESP+convUUID+"/items")
                    .header("Authorization", "Bearer " + openAiApiKey)
                    .body(requestBody)
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {});

            // 응답에서 텍스트 추출
            String result = extractContentFromResponse(response);
            System.out.println("GPT 응답결과 : " + result);

            // 결과를 리스트로 변환하고 검증 -> 검증되지 못한 결과는 empty list로 반환됨
            return parseAndValidateResult(result, categories);

        } catch (Exception e) {
            throw new RuntimeException("이미지 분석 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }
    private List<String> parseAndValidateResult(String gptResponse, List<String> categories) {
        List<String> validFoods = new ArrayList<>();
        String trimmedResponse = gptResponse.trim();

        // "해당없음"인 경우
        if ("해당없음".equals(trimmedResponse)) {
            return validFoods;
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
            return validFoods;
        }

        // 중복 제거
        validFoods = validFoods.stream().distinct().collect(Collectors.toList());
        return validFoods;
    }

    // GPT Conversation 방 없애기 -> 일단 불필요
    /*public boolean deleteConversation(String conversationId) {
        RestTemplate restTemplate = new RestTemplate();
        String url = API_URL + conversationId;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.DELETE, entity, String.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }*/

    // 기존 자유추론 & 문자열 유사도 계산을 통한 음식 레코드 매핑
    /*public List<String> gptAnalyzeImage(String base64Image) {
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
            long start = System.currentTimeMillis();
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

    }*/
    /*
    public List<String> representativeFoodRecordMapper(List<String> gptResponse) {
        List<String> representativeFoods = new ArrayList<>();
        List<String> candidateFoods = foodRepository.findDistinctRepresentativeNames();
        for (String gpt : gptResponse) {
            representativeFoods.add(matchByCosine(gpt, candidateFoods));
        }
        logger.info("대표식품명 코사인유사도 매핑결과 : {}", representativeFoods);
        return representativeFoods;
    }
    private String matchByCosine(String input, List<String> candidates) {
        String bestMatch = null;
        double bestScore = 0.0;
        for (String candidate : candidates) {
            double score = getCosineSimilarity(input, candidate);
            if (score > bestScore) {
                bestScore = score;
                bestMatch = candidate;
            }
        }
        logger.info("대표식품명 코사인유사도 계산결과 : {}, {}점", bestMatch, bestScore);
        return (bestScore >= COSINETHRESHOLD) ?  bestMatch : null;
    }
    private double getCosineSimilarity(String s1, String s2) {
        List<String> words1 = Arrays.asList(s1.split("\\s+"));
        List<String> words2 = Arrays.asList(s2.split("\\s+"));

        Set<String> allWords = new HashSet<>();
        allWords.addAll(words1);
        allWords.addAll(words2);

        List<Integer> vec1 = new ArrayList<>();
        List<Integer> vec2 = new ArrayList<>();

        for (String word : allWords) {
            vec1.add(Collections.frequency(words1, word));
            vec2.add(Collections.frequency(words2, word));
        }

        return cosine(vec1, vec2);
    }
    private double cosine(List<Integer> v1, List<Integer> v2) {
        double dot = 0.0, norm1 = 0.0, norm2 = 0.0;
        for (int i = 0; i < v1.size(); i++) {
            dot += v1.get(i) * v2.get(i);
            norm1 += Math.pow(v1.get(i), 2);
            norm2 += Math.pow(v2.get(i), 2);
        }
        return (norm1 == 0 || norm2 == 0) ? 0.0 : dot / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    public List<String> foodRecordMapper(List<String> analyzedRepresentativeFoods, List<String> gptResponse) {
        List<String> analyzedFoods = new ArrayList<>();
        for (int i = 0; i < analyzedRepresentativeFoods.size(); i++) {
            if (analyzedRepresentativeFoods.get(i) == null) {
                continue;
            } else {
                List<String> candidateFoods = foodRepository.findDistinctNameByRepresentativeFood(analyzedRepresentativeFoods.get(i));
                String matchedFood = matchByLevenshtein(gptResponse.get(i), candidateFoods);
                if ((matchedFood == null) || (matchedFood.equals("식별되지않음"))) {
                    continue;
                } else {
                    analyzedFoods.add(matchedFood);
                }
            }
        }
        logger.info("최종식품명 levenshtein 유사도 매핑결과 : {}", analyzedFoods);
        if (analyzedFoods.isEmpty()) {
            analyzedFoods.add("식별되지않음");
            return analyzedFoods;
        } else {
            return analyzedFoods;
        }
    }
    private String matchByLevenshtein(String input, List<String> candidates) {
        String bestMatch = null;
        double bestScore = 0.0;
        for (String candidate : candidates) {
            double score = getLevenshteinSimilarity(input, candidate);
            if (score > bestScore) {
                bestScore = score;
                bestMatch = candidate;
            }
        }
        logger.info("최종식품명 levenshtein 유사도 계산결과 : {}, {}점", bestMatch, bestScore);
        return bestMatch;
    }
    private double getLevenshteinSimilarity(String s1, String s2) {
        LevenshteinDistance distance = new LevenshteinDistance();
        int maxLength = Math.max(s1.length(), s2.length());
        if (maxLength == 0) { return 1.0; }
        int editDistance = distance.apply(s1, s2);
        return 1.0 - ((double) editDistance / maxLength);
    }
    */

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

    @Transactional
    public void deleteMealInfo(MealInfo mealInfo, User user) {
        try {
            mealInfo.setUser(null);
            mealInfoRepository.delete(mealInfo);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Transactional
    public void createFoodMealInfoRelation(String foodName, Long mealInfoId) {
        try {
            Food food = foodRepository.findFirstByName(foodName).orElseThrow(() -> new NoSuchElementException("식품 식별되지않음"));
            MealInfo mealInfo = mealInfoRepository.findById(mealInfoId).orElseThrow(() -> new NoSuchElementException("식단기록없음"));
            food.addMealInfo(mealInfo);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private String extractContentFromResponse(Map<String, Object> response) {
        try {
            List<Map<String, Object>> output = (List<Map<String, Object>>) response.get("output");
            if (output == null || output.isEmpty()) return "해당없음";

            StringBuilder sb = new StringBuilder();
            for (Map<String, Object> msg : output) {
                List<Map<String, Object>> content = (List<Map<String, Object>>) msg.get("content");
                if (content == null) continue;
                for (Map<String, Object> c : content) {
                    Object t = c.get("text");
                    if (t != null) sb.append(t.toString());
                }
            }
            String text = sb.toString().trim();
            return text.isEmpty() ? "해당없음" : text;
        } catch (Exception e) {
            return "해당없음";
        }
    }

    /*private String extractContentFromResponse(Map<String, Object> response) {
        try {
            List<Map<String, Object>> choices =
                    (List<Map<String, Object>>) response.get("output");

            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> firstChoice = choices.get(0);
                Map<String, Object> message =
                        (Map<String, Object>) firstChoice.get("content");

                if (message != null) {
                    return (String) message.get("content");
                }
            }

            return "해당없음";
        } catch (Exception e) {
            return "해당없음";
        }
    }*/
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
