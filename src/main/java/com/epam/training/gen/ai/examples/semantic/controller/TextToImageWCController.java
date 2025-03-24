package com.epam.training.gen.ai.examples.semantic.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/imagewc")
public class TextToImageWCController {
    private static final String API_URL = "https://ai-proxy.lab.epam.com/openai/deployments/dall-e-3/chat/completions?api-version=2023-12-01-preview";

    @Autowired
    private  HttpClient httpClient;

    @Autowired
    private  ObjectMapper objectMapper;

    @Value("${client-azureopenai-key}")
    private String apiKey;

    @GetMapping
    public String generateImage(@RequestParam String prompt) throws IOException, InterruptedException {
        var requestBody = new HashMap<>();
        requestBody.put("messages", new Object[]{
                Map.of("role", "user", "content", "A drawing of the simple cat")
        });
        requestBody.put("max_tokens", 1000);

        var request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/json")
                .header("Api-Key", apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(requestBody)))
                .build();

        var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            var jsonResponse = objectMapper.readTree(response.body());
            return jsonResponse.at("/choices/0/message/custom_content/attachments/1/url").asText();
        } else {
            throw new RuntimeException("Failed to generate image: " + response.body());
        }
    }

}
