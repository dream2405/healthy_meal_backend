package kr.ac.dankook.ace.healthy_meal_backend;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("건강한 끼니 API")
                        .version("v0.1.0")
                        .description("건강한 끼니 프로젝트 REST API"));
    }
}
