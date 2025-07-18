package kr.ac.dankook.ace.healthy_meal_backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

@Data
@NoArgsConstructor
public class UserPostDTO {
    private String id;
    private String hashedPassword;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthday;
    private String gender;
}
