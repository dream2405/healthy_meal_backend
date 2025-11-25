package kr.ac.dankook.ace.healthy_meal_backend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;

@Data
public class DailyScoreElement {
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;
    private Integer dailyscore;
}
