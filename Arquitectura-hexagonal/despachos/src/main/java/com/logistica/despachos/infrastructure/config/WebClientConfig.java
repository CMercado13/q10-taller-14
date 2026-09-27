package com.logistica.despachos.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient.Builder webClientBuilder(
            @org.springframework.beans.factory.annotation.Value("${app.base-url:http://localhost:8081}") String baseUrl) {
        return WebClient.builder().baseUrl(baseUrl);
    }
}