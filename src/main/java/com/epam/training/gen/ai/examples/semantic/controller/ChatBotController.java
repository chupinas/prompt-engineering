package com.epam.training.gen.ai.examples.semantic.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.orchestration.InvocationContext;
import com.microsoft.semantickernel.orchestration.PromptExecutionSettings;
import com.microsoft.semantickernel.services.chatcompletion.ChatCompletionService;
import com.microsoft.semantickernel.services.chatcompletion.ChatHistory;
import com.microsoft.semantickernel.services.chatcompletion.ChatMessageContent;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/chat")
public class ChatBotController {

    @Autowired
    private ChatCompletionService chatCompletionService;

    @Autowired
    private Kernel kernel;

    @Autowired
    private InvocationContext invocationContext;

    @Autowired
    private ObjectMapper objectMapper;

    @GetMapping
    public Map<String, String> getChatbotResponse(@RequestParam String input) {
        ChatHistory history = new ChatHistory();

        history.addUserMessage(input);
        List<ChatMessageContent<?>> response = chatCompletionService.getChatMessageContentsAsync(
                history,
                kernel,
                InvocationContext.builder()
                        .withPromptExecutionSettings(PromptExecutionSettings.builder()
                                .withTemperature(0.1)
                                .build())
                        .build()
        ).block();
        history.addAll(response);

        history.addUserMessage(input);
        List<ChatMessageContent<?>> creativeResponse = chatCompletionService.getChatMessageContentsAsync(
                history,
                kernel,
                InvocationContext.builder()
                        .withPromptExecutionSettings(PromptExecutionSettings.builder()
                                .withTemperature(1.0)
                                .build())
                        .build()
        ).block();
        history.addAll(creativeResponse);

        return Map.of("input t=0.1", input,
                "response t=0.1", convertChatMessagesToJson(response),
                "input t=1.0", input,
                "response t=1.0", convertChatMessagesToJson(creativeResponse));
    }

//    {
//        "response t=0.1": "[\"I'm sorry, I am an AI assistant and I do not have real-time information. I recommend checking a reliable weather website or app for the most up-to-date weather information in Krakow.\"]",
//        "response t=1.0": "[\"I'm sorry for my previous response. Let me check the weather for you. \\n\\nAs of now, the weather in Krakow is mostly cloudy with a high of 21°C and a low of 9°C. There is a chance of rain showers throughout the day. Make sure to check a reliable weather source for the most up-to-date information.\"]",
//            "input t=1.0": "what the weather today in krakow",
//            "input t=0.1": "what the weather today in krakow"
//    }

    private String convertChatMessagesToJson(List<ChatMessageContent<?>> messages) {
        try {
            return objectMapper.writeValueAsString(messages.stream().map(msg -> msg.getContent()).toList());
        } catch (Exception e) {
            throw new RuntimeException("Error converting messages to JSON", e);
        }
    }
}
