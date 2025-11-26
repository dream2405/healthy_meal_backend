package kr.ac.dankook.ace.healthy_meal_backend.config;

import kr.ac.dankook.ace.healthy_meal_backend.dto.MealRecordDTO;
import kr.ac.dankook.ace.healthy_meal_backend.dto.NutrientValuesDTO;
import kr.ac.dankook.ace.healthy_meal_backend.entity.Food;
import kr.ac.dankook.ace.healthy_meal_backend.entity.MealRecord;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {
    @Bean
    public ModelMapper modelMapper() {
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STANDARD);
        mapper.createTypeMap(MealRecord.class, MealRecordDTO.class)
                .addMappings(mapping -> mapping.map(MealRecord::getId, MealRecordDTO::setId));
        return mapper;
    }
}
