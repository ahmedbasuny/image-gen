package com.ai.image;

import com.google.genai.Client;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GenAIConfiguration {

    @Bean
    public Client genaiClient(@Value("${google.api.key}") String apiKey) {
        return Client.builder()
                .apiKey(apiKey)
                .build();
    }
}
