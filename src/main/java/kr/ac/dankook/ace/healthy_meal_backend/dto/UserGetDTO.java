package kr.ac.dankook.ace.healthy_meal_backend.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UserGetDTO {
    private String id;
    private String hashedPassword;
    private LocalDate birthday;
    private Character gender;
}
