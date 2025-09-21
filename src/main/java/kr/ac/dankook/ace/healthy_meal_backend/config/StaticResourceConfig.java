package kr.ac.dankook.ace.healthy_meal_backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.Duration;

@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    @Value("${storage.location}")
    private String DEFAULT_STORAGE_LOCATION;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")      // /uploads/로 접근하는 API에 정적파일 리턴해줌..
                .addResourceLocations("file:"+DEFAULT_STORAGE_LOCATION)
                .setCacheControl(CacheControl.maxAge(Duration.ofDays(30)));
    }
}
