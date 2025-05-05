package com.epam.training.gen.ai.examples.semantic.controller;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.azure.ai.openai.models.Embeddings;
import com.azure.ai.openai.models.EmbeddingsOptions;
import com.epam.training.gen.ai.examples.semantic.model.DummyRecord;
import com.microsoft.semantickernel.aiservices.openai.textembedding.OpenAITextEmbeddingGenerationService;
import com.microsoft.semantickernel.data.vectorsearch.VectorSearchResults;
import com.microsoft.semantickernel.data.vectorstorage.VectorStoreRecordCollection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Slf4j
@RestController
@RequestMapping("/api/embeddings")
public class EmbeddingController {

    @Autowired
    private OpenAIAsyncClient openAIClient;

    @Autowired
    private VectorStoreRecordCollection<String, DummyRecord> vectorStoreRecordCollection;

    @Autowired
    private OpenAITextEmbeddingGenerationService embeddingGeneration;

    @Value("${client-azureopenai-deployment-name}")
    private String deploymentName;



    /**
     * Endpoint to generate embeddings for a provided text.
     *
     * @param input The input text for which an embedding should be generated.
     * @return A JSON response with the embedding vector.
     */
    @PostMapping("/generate")
    public Map<String, Object> generateEmbedding(@RequestBody Map<String, String> requestBody) {
        String inputText = requestBody.get("input");
        if (inputText == null || inputText.isBlank()) {
            return Map.of("error", "Input text cannot be null or empty");
        }

        try {
            // Options for embedding generation
            EmbeddingsOptions options = new EmbeddingsOptions(List.of(inputText));

            // Use Azure OpenAI to generate embedding
            Embeddings embeddings = openAIClient.getEmbeddings(deploymentName, options).block();

            // Extract embedding vector (first element in response)
            List<Float> embedding = embeddings.getData().get(0).getEmbedding();
            return Map.of(
                    "input", inputText,
                    "embedding", embedding
            );
        } catch (Exception e) {
            throw new RuntimeException("Error while generating embedding: " + e.getMessage(), e);
        }
    }

    /**
     * Endpoint to generate embeddings for a provided text and saves it.
     *
     * @param input The input text for which an embedding should be generated.
     * @return A JSON response with the embedding vector.
     */
    @PostMapping("/generateAndSave")
    public Map<String, String> generateAndSaveEmbedding(@RequestBody Map<String, String> requestBody) {
        var inputText = requestBody.get("input");
        if (inputText == null || inputText.isBlank()) {
            return Map.of("error", "Input text cannot be null or empty");
        }

        // Options for embedding generation
        var options = new EmbeddingsOptions(List.of(inputText));
        // Use Azure OpenAI to generate embedding

        vectorStoreRecordCollection.createCollectionIfNotExistsAsync()
                .then(embeddingGeneration.generateEmbeddingAsync(inputText)
                        .flatMap(embedding -> vectorStoreRecordCollection.upsertAsync(
                                new DummyRecord(
                                        inputText, //DummyRecord.encodeId(inputText),
                                        embedding.getVector()
                                ), null
                        ))).block();
        return Map.of("Saved input", inputText);
    }

    @PostMapping("/search")
    public Map<String, String> search(@RequestBody Map<String, String> requestBody) throws ExecutionException, InterruptedException {
        var inputText = requestBody.get("input");
        if (inputText == null || inputText.isBlank()) {
            return Map.of("error", "Input text cannot be null or empty");
        }
        // Search for results
        // Volatile store executes an exhaustive search, for approximate search use Azure AI Search, Redis or JDBC with PostgreSQL
        var results = search(inputText, vectorStoreRecordCollection, embeddingGeneration).block();

        if (results == null || results.getTotalCount() == 0) {
            return Map.of("No search results found.", inputText);
        }
        var searchResult = results.getResults().get(0);
        return Map.of(inputText, String.format("Search result with score: %f.%n Link: %s",
                searchResult.getScore(), searchResult.getRecord().getId()));
    }

    private static Mono<VectorSearchResults<DummyRecord>> search(
            String searchText,
            VectorStoreRecordCollection<String, DummyRecord> recordCollection,
            OpenAITextEmbeddingGenerationService embeddingGeneration) {
        // Generate embeddings for the search text and search for the closest records
        return embeddingGeneration.generateEmbeddingAsync(searchText)
                .flatMap(r -> recordCollection.searchAsync(r.getVector(), null));
    }
}
