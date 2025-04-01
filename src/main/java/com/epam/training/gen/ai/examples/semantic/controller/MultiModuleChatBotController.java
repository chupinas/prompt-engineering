package com.epam.training.gen.ai.examples.semantic.controller;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.epam.training.gen.ai.examples.semantic.configuration.SemanticKernelConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.aiservices.openai.chatcompletion.OpenAIChatCompletion;
import com.microsoft.semantickernel.orchestration.InvocationContext;
import com.microsoft.semantickernel.orchestration.PromptExecutionSettings;
import com.microsoft.semantickernel.services.chatcompletion.ChatCompletionService;
import com.microsoft.semantickernel.services.chatcompletion.ChatHistory;
import com.microsoft.semantickernel.services.chatcompletion.ChatMessageContent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("/mmchat")
public class MultiModuleChatBotController {

    @Autowired
    private Kernel kernel;

    @Autowired
    private InvocationContext invocationContext;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private List<SemanticKernelConfiguration.Model> deployedModels;

    @Autowired
    private OpenAIAsyncClient openAIAsyncClient;

    @GetMapping
    public Map<String, String> getMMChatbotResponse(@RequestParam String input) {
        ChatHistory history = new ChatHistory();

        SemanticKernelConfiguration.Model model1 = deployedModels.get(getRandomIndex(deployedModels.size() - 1));
        history.addUserMessage(input);
        List<ChatMessageContent<?>> response1 = getChatMessageContents(model1.getId(), history);
        history.addAll(response1);

        SemanticKernelConfiguration.Model model2 = deployedModels.get(getRandomIndex(deployedModels.size() - 1));
        history.addUserMessage(input);
        List<ChatMessageContent<?>> response2 = getChatMessageContents(model2.getId(), history);
        history.addAll(response2);

        return Map.of("input of " + model1.getId(), input,
                "response of " + model1.getId(), convertChatMessagesToJson(response1),
                "input of " + model2.getId(), input,
                "response of " + model2.getId(), convertChatMessagesToJson(response2));
    }

    private List<ChatMessageContent<?>> getChatMessageContents(String modelId, ChatHistory chatHistory){
        OpenAIChatCompletion openAIChatCompletion = OpenAIChatCompletion.builder()
                .withModelId(modelId)
                .withOpenAIAsyncClient(openAIAsyncClient)
                .build();
        return OpenAIChatCompletion.builder()
                .withModelId(modelId)
                .withOpenAIAsyncClient(openAIAsyncClient)
                .build().getChatMessageContentsAsync(
                        chatHistory,
                        kernel.toBuilder().withAIService(ChatCompletionService.class, openAIChatCompletion).build(),
                        invocationContext
                ).block();
    }

    private int getRandomIndex(int maxIndex) {
        return new Random().ints(0, maxIndex).findFirst().getAsInt();
    }


    private String convertChatMessagesToJson(List<ChatMessageContent<?>> messages) {
        try {
            return objectMapper.writeValueAsString(messages.stream().map(msg -> msg.getContent()).toList());
        } catch (Exception e) {
            throw new RuntimeException("Error converting messages to JSON", e);
        }
    }
}
