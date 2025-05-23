package com.epam.training.gen.ai.examples.semantic.configuration;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.MetricsOptions;
import com.azure.core.util.TracingOptions;
import com.azure.search.documents.indexes.SearchIndexAsyncClient;
import com.azure.search.documents.indexes.SearchIndexClientBuilder;
import com.epam.training.gen.ai.examples.semantic.model.DummyRecord;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.aiservices.openai.chatcompletion.OpenAIChatCompletion;
import com.microsoft.semantickernel.aiservices.openai.textembedding.OpenAITextEmbeddingGenerationService;
import com.microsoft.semantickernel.data.VolatileVectorStore;
import com.microsoft.semantickernel.data.VolatileVectorStoreRecordCollectionOptions;
import com.microsoft.semantickernel.data.vectorstorage.VectorStoreRecordCollection;
import com.microsoft.semantickernel.orchestration.InvocationContext;
import com.microsoft.semantickernel.orchestration.PromptExecutionSettings;
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

    private static final int EMBEDDING_DIMENSIONS = 0;

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

    @Bean
    public OpenAITextEmbeddingGenerationService embeddingGeneration(@Value("${client-azureopenai-deployment-name}")
                                                                    String deploymentOrModelName,
                                                                    OpenAIAsyncClient openAIAsyncClient){
        return OpenAITextEmbeddingGenerationService.builder()
                .withOpenAIAsyncClient(openAIAsyncClient)
                .withModelId(deploymentOrModelName)
                .withDimensions(EMBEDDING_DIMENSIONS)
                .build();
    }

    @Bean
    public SearchIndexAsyncClient searchClient(@Value("${client-azureopenai-key}") String openaiKey,
                                               @Value("${client-azureopenai-endpoint}") String openaiEndpoint){
        return new SearchIndexClientBuilder()
                .endpoint(openaiEndpoint)
                .credential(new AzureKeyCredential(openaiKey))
                .clientOptions(new ClientOptions()
                        .setTracingOptions(new TracingOptions())
                        .setMetricsOptions(new MetricsOptions())
                        .setApplicationId("Semantic-Kernel"))
                .buildAsyncClient();
    }

    @Bean
    public VectorStoreRecordCollection<String, DummyRecord> vectorStoreRecordCollection(){
        var volatileVectorStore = new VolatileVectorStore();
        String collectionName = "testDataStore";
        return volatileVectorStore.getCollection(collectionName,
                VolatileVectorStoreRecordCollectionOptions.<DummyRecord>builder()
                        .withRecordClass(DummyRecord.class)
                        .build());
    }
}

