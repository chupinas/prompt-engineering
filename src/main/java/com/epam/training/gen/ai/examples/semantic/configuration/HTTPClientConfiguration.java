package com.epam.training.gen.ai.examples.semantic.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.http.HttpClient;

@Configuration
public class HTTPClientConfiguration {

    @Bean
    public HttpClient httpClient(@Value("${client-azureopenai-key}") String openaiKey,
                                 @Value("${client-azureopenai-endpoint}") String openaiEndpoint){
        return HttpClient.newHttpClient();
    }

}
