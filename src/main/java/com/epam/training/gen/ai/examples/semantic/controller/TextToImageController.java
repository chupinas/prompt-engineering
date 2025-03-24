package com.epam.training.gen.ai.examples.semantic.controller;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.azure.ai.openai.models.ImageGenerationData;
import com.azure.ai.openai.models.ImageGenerationOptions;
import com.azure.ai.openai.models.ImageGenerations;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/image")
public class TextToImageController {

    @Autowired
    private OpenAIAsyncClient openAIAsyncClient;

    @Autowired
    private ObjectMapper objectMapper;

    @GetMapping
    public Map<String, String> generateImage(@RequestParam String prompt) throws IOException, InterruptedException {
        ImageGenerations images = openAIAsyncClient.getImageGenerations("dall-e-3", new ImageGenerationOptions(prompt)).block();
        for (ImageGenerationData imageGenerationData : images.getData()) {
            return Map.of("imgUrl:", imageGenerationData.getUrl());
        }
        return Map.of();
    }
}
