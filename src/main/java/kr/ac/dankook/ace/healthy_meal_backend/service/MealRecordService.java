package kr.ac.dankook.ace.healthy_meal_backend.service;

import jakarta.transaction.Transactional;
import kr.ac.dankook.ace.healthy_meal_backend.dto.FoodElement;
import kr.ac.dankook.ace.healthy_meal_backend.dto.MealRecordDTO;
import kr.ac.dankook.ace.healthy_meal_backend.dto.MealRecordRequestDTO;
import kr.ac.dankook.ace.healthy_meal_backend.entity.*;
import kr.ac.dankook.ace.healthy_meal_backend.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MealRecordService {
    @Value("${openai.api.key}")
    private String openAiApiKey;
    private static final String OPENAI_API_URL = "https://api.openai.com/v1/responses";
    private static final String OPENAI_CONV = "https://api.oepnai.com/v1/conversations";
    private static final String MODEL4 = "gpt-4o";
    private static final String MODEL5 = "gpt-5";
    private final FoodRepository foodRepository;
    private final MealRecordRepository mealRecordRepository;
    private final DailyIntakeRepository dailyIntakeRepository;
    private final UserRepository userRepository;
    private final MealRecordFoodLinkRepository mealRecordFoodLinkRepository;
    private final RestClient convClient;
    private final RestClient respClient;
    private final ImagePreprocessorService imagePreprocessorService;
    private final NutritionService nutritionService;
    private final NutrientIntakeService nutrientIntakeService;

    private static final Logger logger = LoggerFactory.getLogger(MealRecordService.class);

    @Autowired
    public MealRecordService(
            MealRecordRepository mealRecordRepository,
            FoodRepository foodRepository,
            UserRepository userRepository,
            DailyIntakeRepository dailyIntakeRepository,
            MealRecordFoodLinkRepository mealRecordFoodLinkRepository,
            NutrientWeightRepository nutrientWeightRepository,
            @Qualifier("convClient") RestClient convClient,
            @Qualifier("respClient") RestClient respClient,
            NutritionService nutritionService,
            NutrientIntakeService nutrientIntakeService,
            ImagePreprocessorService imagePreprocessorService
    ) {
        this.mealRecordRepository = mealRecordRepository;
        this.respClient = respClient;
        this.convClient = convClient;
        this.foodRepository = foodRepository;
        this.userRepository = userRepository;
        this.mealRecordFoodLinkRepository = mealRecordFoodLinkRepository;
        this.dailyIntakeRepository = dailyIntakeRepository;
        this.nutritionService = nutritionService;
        this.imagePreprocessorService = imagePreprocessorService;
        this.nutrientIntakeService = nutrientIntakeService;
    }

    public List<MealRecordDTO> getMealRecord(String userId, LocalDate date) {
        List<MealRecordDTO> mealRecordDTOS = new ArrayList<>();

        List<MealRecord> mealRecords = mealRecordRepository.findByUserIdAndCreatedDate(userId, date);
        for (MealRecord mealRecord : mealRecords) {
            MealRecordDTO mealRecordDTO = new MealRecordDTO();
            List<MealRecordFoodLink> foodLinks = mealRecordFoodLinkRepository.findByMealRecord(mealRecord);
            mealRecordDTO.setId(mealRecord.getId());
            mealRecordDTO.setMealName(mealRecord.getMealName());
            mealRecordDTO.setImgPath(mealRecord.getImgPath());
            mealRecordDTO.setTakenAt(mealRecord.getTakenAt());
            mealRecordDTO.setDiary(mealRecord.getDiary());
            mealRecordDTO.setFoods(getFoodElements(foodLinks));
            mealRecordDTOS.add(mealRecordDTO);
        }
        return mealRecordDTOS;
    }
    private List<FoodElement> getFoodElements(List<MealRecordFoodLink> foodLinks) {
        List<FoodElement> foodElements = new ArrayList<>();
        for (MealRecordFoodLink foodLink : foodLinks) {
            FoodElement foodElement = new FoodElement();
            Food food = foodLink.getFood();
            Float intakeAmount = foodLink.getIntakeAmount();
            foodElement.setName(food.getName());
            foodElement.setIntakeAmount(intakeAmount);
            foodElements.add(setNutrientValue(food, foodElement));
        }
        return foodElements;
    }
    private FoodElement setNutrientValue(Food e, FoodElement dto) {
        dto.setEnergyKcal(e.getEnergyKcal());
        dto.setCarbohydrateG(e.getCarbohydrateG());
        dto.setProteinG(e.getProteinG());
        dto.setCalciumMg(e.getCalciumMg());
        dto.setKaliumMg(e.getKaliumMg());
        dto.setIronMg(e.getIronMg());
        dto.setMagnesiumMg(e.getMagnesiumMg());
        dto.setZincMg(e.getZincMg());
        dto.setCelluloseG(e.getCelluloseG());
        dto.setAminoacidMg(e.getAminoacidMg());
        dto.setLeucineMg(e.getLeucineMg());
        dto.setMethionineMg(e.getMethionineMg());
        dto.setSeleniumUg(e.getSeleniumUg());
        dto.setOmega3G(e.getOmega3G());
        dto.setVitaminAUg(e.getVitaminAUg());
        dto.setVitaminBMg(e.getVitaminBMg());
        dto.setFolicacidUg(e.getFolicacidUg());
        dto.setVitaminB12Ug(e.getVitaminB12Mg());
        dto.setVitaminCMg(e.getVitaminCMg());
        dto.setVitaminDUg(e.getVitaminDUg());
        dto.setVitaminEMg(e.getVitaminEMg());
        return dto;
    }
    /*
    private double getNutrientValue(Food food, String fieldName) {
        try {
            // 1. 해당 클래스에서 이름이 일치하는 필드(Field) 객체를 찾음
            Field field = FoodElement.class.getDeclaredField(fieldName);

            // 2. private 필드여도 접근 가능하도록 설정
            field.setAccessible(true);

            return ((Number) field.get(food)).doubleValue();
        } catch (NoSuchFieldException e) {
            System.out.println("필드 이름을 찾을 수 없습니다: " + fieldName);
            return 0;
        } catch (IllegalAccessException e) {
            System.out.println("필드에 접근할 수 없습니다: " + fieldName);
            return 0;
        }
    }
    private void setNutrientValue(FoodElement element, String fieldName, Double value) {
        try {
            // 1. 해당 클래스에서 이름이 일치하는 필드(Field) 객체를 찾음
            Field field = FoodElement.class.getDeclaredField(fieldName);

            // 2. private 필드여도 접근 가능하도록 설정
            field.setAccessible(true);

            // 3. 해당 객체(element)의 필드에 값(value) 설정
            field.set(element, value);

        } catch (NoSuchFieldException e) {
            System.out.println("필드 이름을 찾을 수 없습니다: " + fieldName);
        } catch (IllegalAccessException e) {
            System.out.println("필드에 접근할 수 없습니다: " + fieldName);
        }
    }*/

    @Transactional
    public MealRecord createMealRecord(String userId, MealRecordRequestDTO mealRecordRequestDTO) {
        MealRecord mealRecord = new MealRecord();
        mealRecord.setMealName(mealRecordRequestDTO.getMealName());
        mealRecord.setImgPath(mealRecordRequestDTO.getImgPath());
        mealRecord.setDiary(mealRecordRequestDTO.getDiary());
        mealRecord.setTakenAt(mealRecordRequestDTO.getTakenAt());
        mealRecord.setCreatedAt(LocalDateTime.now());
        mealRecord.setLastModifiedAt(LocalDateTime.now());
        User user = userRepository.findById(userId).orElseThrow(RuntimeException::new);
        mealRecord.setUser(user);
        mealRecordRepository.save(mealRecord);

        DailyIntake dailyIntake = nutrientIntakeService.getDailyIntake(user, LocalDate.now());
        List<String> foodNames = mealRecordRequestDTO.getConfirmedFoods();
        List<Float> intakeAmounts = mealRecordRequestDTO.getIntakeAmounts();
        for (int i = 0; i < foodNames.size(); i++) {
            Food food = foodRepository.findFirstByName(foodNames.get(i)).orElseThrow(NoSuchElementException::new);
            MealRecordFoodLink mealRecordFoodLink = new MealRecordFoodLink();
            mealRecordFoodLink.setFood(food);
            mealRecordFoodLink.setMealRecord(mealRecord);
            mealRecordFoodLink.setIntakeAmount(intakeAmounts.get(i));
            nutrientIntakeService.addFoodNutrition(dailyIntake, food, intakeAmounts.get(i));
            System.out.println(mealRecordFoodLink);
            mealRecordFoodLinkRepository.save(mealRecordFoodLink);
        }
        dailyIntakeRepository.save(dailyIntake);
        nutrientIntakeService.calcDailyIntakeScore(userId, nutritionService.getDietCriterion(userId).getNutrientValues());
        return mealRecord;
    }

    @Transactional
    public MealRecord patchMealRecord(String userId, MealRecordRequestDTO dto) {
        // 1. MealRecord Entity 조회
        MealRecord mealRecord = mealRecordRepository.findById(dto.getId()).orElseThrow(() -> new NoSuchElementException("해당 request의 mealrecord id에 대한 식단기록이 없습니다"));
        if (!Objects.equals(mealRecord.getUser().getId(), userId)) {
            throw new IllegalArgumentException("본인의 기록만 수정할 수 있습니다");
        }
        // 2. 기본 정보 업데이트
        mealRecord.setMealName(dto.getMealName());
        mealRecord.setDiary(dto.getDiary());
        mealRecord.setTakenAt(dto.getTakenAt());
        mealRecord.setLastModifiedAt(LocalDateTime.now());

        // 3. DailyIntake 초기화
        nutrientIntakeService.applyDeleteDailyIntake(mealRecord, userId, dto.getTakenAt().toLocalDate());
        DailyIntake dailyIntake = dailyIntakeRepository.findByUserIdAndDay(userId, dto.getTakenAt().toLocalDate()).orElseThrow(() -> new NoSuchElementException("해당 날짜의 영양 섭취 기록이 없습니다."));
        List<MealRecordFoodLink> existingFoods = mealRecordFoodLinkRepository.findByMealRecord(mealRecord);

        mealRecordRepository.save(mealRecord);
        return mealRecord;
    }

    @Transactional
    public void deleteMealRecord(String userId, Long mealRecordId) {
        MealRecord mealRecord = mealRecordRepository.findById(mealRecordId).orElseThrow(() -> new NoSuchElementException("해당 request의 mealrecord id에 대한 식단기록이 없습니다"));
        if (!Objects.equals(mealRecord.getUser().getId(), userId)) {
            throw new IllegalArgumentException("본인의 기록만 삭제할 수 있습니다");
        }
        nutrientIntakeService.applyDeleteDailyIntake(mealRecord, userId, mealRecord.getTakenAt().toLocalDate());
        mealRecordRepository.delete(mealRecord);
    }

    public List<String> gptAnalyzeImage(String base64Image) {
        long start = System.currentTimeMillis();

        // === 이미지 전처리: base64 원본 → 두 가지 압축 버전 생성 ===
        String smallB64; // 1차(대분류) 용: 896px / Q≈0.65
        String largeB64; // 2,3차(대표/최종) 용: 1024px / Q≈0.74
        try {
            byte[] original = imagePreprocessorService.base64ToBytes(base64Image);

            // 1) 1차용
            smallB64 = imagePreprocessorService.toJpegBase64(original, 896, 0.65f);
            // 너무 작게 나왔으면(예: <80KB) Q를 살짝 올려 한 번 보정
            if (Base64.getDecoder().decode(smallB64).length < 80_000) {
                smallB64 = imagePreprocessorService.toJpegBase64(original, 896, 0.72f);
            }

            // 2) 2·3차용
            largeB64 = imagePreprocessorService.toJpegBase64(original, 1024, 0.74f);
            // 너무 크면(>300KB) Q 낮춰 한 번 보정
            if (Base64.getDecoder().decode(largeB64).length > 300_000) {
                largeB64 = imagePreprocessorService.toJpegBase64(original, 1024, 0.68f);
            }
        } catch (Exception e) {
            logger.warn("이미지 전처리 실패, 원본 사용: {}", e.getMessage());
            smallB64 = base64Image;
            largeB64 = base64Image;
        }
// === 전처리 끝 ===
        base64Image = largeB64;
        String imageID = UUID.randomUUID().toString();

        int foodCount = 0;
        List<String> majorCategories = foodRepository.findDistinctMajorCategoryNative();
        List<String> majorCategoriesResult;
        List<String> representativeFoods = new ArrayList<>();
        List<String> representativeFoodsResult;
        List<String> foods = new ArrayList<>();
        List<String> foodResult = new ArrayList<>();

        // Conversation(대화방) 생성
        String convID;
        convID = createAnalyze();

        // 첫번째 gpt 분석 - 식단 이미지로 맞는 대분류 매칭 -> firstanalyzeImage()
        majorCategoriesResult = firstanalyzeImage(base64Image, majorCategories, convID);
        foodCount = majorCategoriesResult.size();
        if (majorCategoriesResult.isEmpty()) {
            System.out.println("대분류 분석실패");
            return foodResult;
        }
        for (String majorCategory : majorCategoriesResult) {
            representativeFoods.addAll(foodRepository.findDistinctRepresentativeFoodByMajorCategory(majorCategory));
        }
        long mid1 = System.currentTimeMillis();
        logger.info("GPT 대분류식별 소요시간 : {} s", (mid1 - start)/1000);

        // 두번째 gpt 분석 - 대표 음식 매칭 - analyzeImage()
        representativeFoodsResult = analyzeImage(representativeFoods, foodCount, convID);
        if (representativeFoodsResult.isEmpty()) {
            System.out.println("대표식품명 분석실패");
            return foodResult;
        }
        for (String representativeFood : representativeFoodsResult) {
            foods.addAll(foodRepository.findDistinctNameByRepresentativeFood(representativeFood));
        }
        long mid2 = System.currentTimeMillis();
        logger.info("GPT 대표식품식별 소요시간 : {} s", (mid2 - start)/1000);

        // 마지막 gpt 분석 - 최종 음식 매칭 - analyzeImage()
        foodResult = finalanalyzeImage(foods, foodCount, convID);

        long end = System.currentTimeMillis();
        logger.info("GPT 사진분석 소요시간 : {} s", (end - start)/1000);

        return foodResult;
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
            Map<String, Object> response = convClient.post()
                    .header("Authorization", "Bearer " + openAiApiKey)
                    .body(requestBody)
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {
                    });

            // 응답에서 텍스트 추출
            String result = Objects.requireNonNull(response).get("id").toString();
            System.out.println("생성된대화방 : " + result);

            return result;

        } catch (Exception e) {
            throw new RuntimeException("conv 생성중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }
    private List<String> firstanalyzeImage(String base64Image, List<String> categories, String convID) {
        // 리스트를 문자열로 변환
        String categoriesString = String.join(", ", categories);
        System.out.println("GPT 선제시목록(" + categories.size() + ")개 : " + categoriesString);

        // 프롬프트 생성
        String prompt = String.format(
                """
                        이미지를 보고 다음 음식 목록 중에서 이미지에 있는 모든 음식 종류를 찾아주세요.
                        
                        음식 목록: %s
                        
                        규칙:
                        1. 반드시 제공된 음식 목록에서만 선택
                        2. 반찬이나 소스류를 제외하고 주요 요리 카테고리만 작성
                        3. 여러 음식이면 쉼표(,)로 구분
                        4. 답변은 카테고리 이름만, 추가 설명 없음
                        5. 답변 예시: '김치찌개, 밥, 계란후라이' 또는 '피자'
                        6. 이미지 식별 실패시 그 이유를 작성
                        
                        예외:
                        
                        
                        """
                ,
                categoriesString
        );

        // 요청 body 구성
        Map<String, Object> requestBody = Map.of(
                    "model", MODEL4,
                    "conversation", convID,
                    "input", List.of(
                            Map.of(
                                    "role", "user",
                                    "content", List.of(
                                            Map.of(
                                                    "type", "input_text",
                                                    "text", prompt
                                            ),
                                            Map.of(
                                                    "type", "input_image",
                                                    "image_url", "data:image/jpeg;base64," + base64Image
                                                    //"image_url", "https://images.unsplash.com/photo-1604382354936-07c5d9983bd3?fm=jpg&q=60&w=3000&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxzZWFyY2h8N3x8cGl6emF8ZW58MHx8MHx8fDA%3D"
                                            )
                                    )
                            )
                    )
                //"max_output_tokens", 1000  // 여러 음식 이름을 위해 토큰 증가
        );

        try {
            // OpenAI API 호출
            Map<String, Object> response = respClient.post()
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
    private List<String> analyzeImage(List<String> categories, int foodCount, String convID) {
        // 리스트를 문자열로 변환
        String categoriesString = String.join(", ", categories);
        System.out.println("GPT 선제시목록(" + categories.size() + ")개 : " + categoriesString);

        // 프롬프트 생성
        String prompt = String.format(
                """
                        이전에 제시했던 이미지에 있는 모든 음식 목록을 찾아주세요.
                        
                        음식 목록: %s
                        
                        규칙:
                        1. 반드시 제공된 음식 목록에서만 선택
                        2. 이미지에서 식별된 %d개의 음식 당 가장 적합한 카테고리를 중복없이 작성
                        3. 여러 음식이면 쉼표(,)로 구분
                        4. 답변은 카테고리 이름만, 추가 설명 없음
                        5. 답변 예시: '김치찌개, 밥, 계란후라이' 또는 '피자'
                        6. 이미지 식별 실패시 그 이유를 작성
                        
                        """,
                categoriesString, foodCount
        );

        // 요청 body 구성
        Map<String, Object> requestBody = Map.of(
                "model", MODEL4,
                "conversation", convID,
                "input", List.of(
                        Map.of(
                                "role", "user",
                                "content", List.of(
                                        Map.of(
                                                "type", "input_text",
                                                "text", prompt
                                        )
                                        /*Map.of(
                                                "type", "input_image",
                                                "image_url", "data:image/jpeg;base64," + base64Image
                                                //"image_url", "https://images.unsplash.com/photo-1604382354936-07c5d9983bd3?fm=jpg&q=60&w=3000&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxzZWFyY2h8N3x8cGl6emF8ZW58MHx8MHx8fDA%3D"
                                        )*/
                                )
                        )
                )
                //"max_output_tokens", 1000  // 여러 음식 이름을 위해 토큰 증가
        );

        try {
            // OpenAI API 호출
            Map<String, Object> response = respClient.post()
                    .header("Authorization", "Bearer " + openAiApiKey)
                    .body(requestBody)
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {});

            // 응답에서 텍스트 추출
            String result = extractContentFromResponse(response);
            System.out.println("GPT 응답결과 : " + result);

            // 결과를 리스트로 변환하고 검증 -> 검증되지 못한 결과는 empty list<string>로 반환됨
            return parseAndValidateResult(result, categories);

        } catch (Exception e) {
            throw new RuntimeException("이미지 분석 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }
    private List<String> finalanalyzeImage(List<String> categories, int foodCount, String convID) {
        // 리스트를 문자열로 변환
        String categoriesString = String.join(", ", categories);
        System.out.println("GPT 선제시목록(" + categories.size() + ")개 : " + categoriesString);

        // 프롬프트 생성
        String prompt = String.format(
                """
                        이전에 제시했던 이미지에 있는 모든 음식 목록을 찾아주세요.
                        
                        음식 목록: %s
                        
                        규칙:
                        1. 반드시 제공된 음식 목록에서만 선택
                        2. 이미지에서 식별된 %d개의 음식 당 가장 적합한 카테고리를 중복없이 작성
                        3. 여러 음식이면 쉼표(,)로 구분
                        4. 답변은 카테고리 이름만, 추가 설명 없음
                        5. 답변 예시: '김치찌개, 밥, 계란후라이' 또는 '피자'
                        6. 이미지 식별 실패시 그 이유를 작성
                        
                        """,
                categoriesString, foodCount
        );

        // 요청 body 구성
        Map<String, Object> requestBody = Map.of(
                "model", MODEL4,
                "conversation", convID,
                "input", List.of(
                        Map.of(
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
            Map<String, Object> response = respClient.post()
                    .header("Authorization", "Bearer " + openAiApiKey)
                    .body(requestBody)
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {});

            // 응답에서 텍스트 추출
            String result = extractContentFromResponse(response);
            System.out.println("GPT 응답결과 : " + result);

            // 결과를 리스트로 변환하고 검증 -> 검증되지 못한 결과는 empty list<string>로 반환됨
            return parseAndValidateResult(result, categories);

        } catch (Exception e) {
            throw new RuntimeException("이미지 분석 중 오류가 발생했습니다: " + e.getMessage(), e);
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
    private List<String> parseAndValidateResult(String gptResponse, List<String> categories) {
        List<String> validFoods = new ArrayList<>();
        String trimmedResponse = gptResponse.trim();        // 문자열 앞뒤공백 제거

        // "해당없음" or "기억없음"인 경우
        if ("해당없음".equals(trimmedResponse) || ("기억없음".equals(trimmedResponse))) {
            System.out.println("GPT 응답결과 : 해당없음 or 기억없음 -> null string 반환");
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
        System.out.println("Valid String: " + validFoods);
        return validFoods;
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

    public List<Integer> getFoodWeight(List<String> foods) {
        List<Integer> foodWeights = new ArrayList<>();
        for (String food : foods) {
            foodRepository.findFirstByName(food).ifPresent(foodRecord -> {
                String raw = Optional.ofNullable(foodRecord.getWeight()).orElse("0");

                // 숫자와 소수점 외 모두 제거
                String numeric = raw.replaceAll("[^\\d.]", "");

                // 빈 문자열 또는 "." 만 남을 경우 대비
                if (numeric.isBlank() || numeric.equals(".")) {
                    numeric = "0";
                }

                try {
                    foodWeights.add(Integer.parseInt(numeric));
                } catch (NumberFormatException e) {
                    // 파싱 실패 시 기본값 사용
                    foodWeights.add(0);
                }
            });
        }
        return foodWeights;
    }
}
