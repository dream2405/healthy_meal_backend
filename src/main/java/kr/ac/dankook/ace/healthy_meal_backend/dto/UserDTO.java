package kr.ac.dankook.ace.healthy_meal_backend.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UserDTO {
    private String id;
    private String email;
    private LocalDate birthday;
    private Character gender;
    private String modelname;
    private Float nutrientLevel;
}
