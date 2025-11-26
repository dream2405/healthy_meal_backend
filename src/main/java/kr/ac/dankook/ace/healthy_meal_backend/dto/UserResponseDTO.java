package kr.ac.dankook.ace.healthy_meal_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDTO {
    private String id;
    private String email;
    private LocalDate birthday;
    private String gender;
    private String modelname;
}
