package kr.ac.dankook.ace.healthy_meal_backend;

import kr.ac.dankook.ace.healthy_meal_backend.dto.FoodDTO;
import kr.ac.dankook.ace.healthy_meal_backend.entity.Food;
import kr.ac.dankook.ace.healthy_meal_backend.entity.MealInfo;
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
                .setMatchingStrategy(MatchingStrategies.STRICT);
/*        mapper.createTypeMap(Food.class, FoodDTO.class)
                .addMappings(mapping -> {
                    mapping.map(Food::getId, FoodDTO::setId);
                });*/
        return mapper;
    }
}
