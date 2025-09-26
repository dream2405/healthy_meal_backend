package kr.ac.dankook.ace.healthy_meal_backend.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class WebClientConfig {

    private static final String OPENAI_RESP = "https://api.openai.com/v1/responses";
    private static final String OPENAI_CONV = "https://api.openai.com/v1/conversations";

    @Bean
    @Qualifier("convClient")
    public RestClient convClient() {
        return RestClient.builder()
                .baseUrl(OPENAI_CONV)
                .defaultHeader("Content-Type", "application/json")
                .requestFactory(createSimpleRequestFactory())
                .build();
    }

    @Bean
    @Qualifier("respClient")
    public RestClient respClient() {
        return RestClient.builder()
                .baseUrl(OPENAI_RESP)
                .build();
    }

    private ClientHttpRequestFactory createSimpleRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(30));  // 연결 타임아웃: 30초
        factory.setReadTimeout(Duration.ofMinutes(5));      // 읽기 타임아웃: 5분
        return factory;
    }

}
