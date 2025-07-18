package kr.ac.dankook.ace.healthy_meal_backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TokenResponseDTO {
    private String token;

    public TokenResponseDTO(String token) {
        this.token = token;
    }
}
