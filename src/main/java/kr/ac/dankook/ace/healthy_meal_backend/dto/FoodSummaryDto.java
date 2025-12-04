package kr.ac.dankook.ace.healthy_meal_backend.dto;

import lombok.Getter;

@Getter
public class FoodSummaryDto {
    private String name;
    private int weight; // 우리는 숫자를 원한다!

    // JPQL에서 호출할 생성자
    public FoodSummaryDto(String name, String rawWeight) {
        this.name = name;
        this.weight = parseWeight(rawWeight);
    }

    // "200g", "150ml", "3.5kg" 등에서 숫자만 추출하는 로직
    private int parseWeight(String rawWeight) {
        if (rawWeight == null) return 0;

        // 1. 정규표현식: 숫자가 아닌 것([^0-9])을 모두 빈 문자열로 치환
        // 예: "200g" -> "200", "약 150g" -> "150"
        String numberOnly = rawWeight.replaceAll("[^0-9]", "");

        // 2. 빈 값 처리 (숫자가 하나도 없는 경우)
        if (numberOnly.isEmpty()) {
            return 0;
        }

        // 3. 정수로 변환
        return Integer.parseInt(numberOnly);
    }
}