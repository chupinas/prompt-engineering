package com.epam.training.gen.ai.examples.semantic.configuration;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.epam.training.gen.ai.examples.semantic.plugin.MultiplyPlugin;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.aiservices.openai.chatcompletion.OpenAIChatCompletion;
import com.microsoft.semantickernel.orchestration.InvocationContext;
import com.microsoft.semantickernel.orchestration.InvocationReturnMode;
import com.microsoft.semantickernel.orchestration.PromptExecutionSettings;
import com.microsoft.semantickernel.orchestration.ToolCallBehavior;
import com.microsoft.semantickernel.plugin.KernelPlugin;
import com.microsoft.semantickernel.plugin.KernelPluginFactory;
import com.microsoft.semantickernel.services.chatcompletion.ChatCompletionService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

/**
 * Configuration class for setting up Semantic Kernel components.
 * <p>
 * This configuration provides several beans necessary for the interaction with
 * Azure OpenAI services and the creation of kernel plugins. It defines beans for
 * chat completion services, kernel plugins, kernel instance, invocation context,
 * and prompt execution settings.
 */
@Configuration
public class SemanticKernelConfiguration {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private  HttpClient httpClient;

    /**
     * Creates a {@link ChatCompletionService} bean for handling chat completions using Azure OpenAI.
     *
     * @param deploymentOrModelName the Azure OpenAI deployment or model name
     * @param openAIAsyncClient the {@link OpenAIAsyncClient} to communicate with Azure OpenAI
     * @return an instance of {@link ChatCompletionService}
     */
    @Bean
    public ChatCompletionService chatCompletionService(@Value("${client-azureopenai-deployment-name}") String deploymentOrModelName,
                                                       OpenAIAsyncClient openAIAsyncClient) {
        return OpenAIChatCompletion.builder()
                .withModelId(deploymentOrModelName)
                .withOpenAIAsyncClient(openAIAsyncClient)
                .build();
    }

    /**
     * Creates a {@link Kernel} bean to manage AI services and plugins.
     *
     * @param chatCompletionService the {@link ChatCompletionService} for handling completions
     * @return an instance of {@link Kernel}
     */
    @Bean
    public Kernel kernel(ChatCompletionService chatCompletionService) {
        return Kernel.builder()
                .withAIService(ChatCompletionService.class, chatCompletionService)
                .withPlugin(KernelPluginFactory.createFromObject(new MultiplyPlugin(),
                        "MultiplyPlugin"))
                .build();
    }

    /**
     * Creates an {@link InvocationContext} bean with default prompt execution settings.
     *
     * @return an instance of {@link InvocationContext}
     */
    @Bean
    public InvocationContext invocationContext() {
        return InvocationContext.builder()
                .withPromptExecutionSettings(PromptExecutionSettings.builder()
                        .withTemperature(1.0)
                        .build())
                .withReturnMode(InvocationReturnMode.LAST_MESSAGE_ONLY)
                .withToolCallBehavior(ToolCallBehavior.allowAllKernelFunctions(true))
                .build();
    }

    /**
     * Creates a map of {@link PromptExecutionSettings} for different models.
     *
     * @param deploymentOrModelName the Azure OpenAI deployment or model name
     * @return a map of model names to {@link PromptExecutionSettings}
     */
    @Bean
    public Map<String, PromptExecutionSettings> promptExecutionsSettingsMap(@Value("${client-azureopenai-deployment-name}")
                                                                            String deploymentOrModelName) {
        return Map.of(deploymentOrModelName, PromptExecutionSettings.builder()
                .withTemperature(1.0)
                .build());
    }


    @Bean
    public List<Model> deployedModels(@Value("${client-azureopenai-endpoint-model-list}") String modelListUrl, @Value("${client-azureopenai-key}") String accessToken) throws IOException, InterruptedException {
        var request = HttpRequest.newBuilder()
                .uri(URI.create(modelListUrl))
                .header("Content-Type", "application/json")
                .header("Api-Key", accessToken)
                .build();
        var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return objectMapper.readValue(response.body(), ModelListResponse.class).getData();
    }

    @Data
    public static class ModelListResponse {
        List<Model> data;
    }

    @Data
    public static class Model {
        String id;
        String model;
        String display_name;
        String icon_url;
        String description;
        String reference;
        String owner;
        String object;
        String status;
        long created_at;
        long updated_at;
        Map<String, Boolean> features;
        Map<String, Object> defaults;
        List<String> description_keywords;
        int max_retry_attempts;
        String lifecycle_status;
    }
}

