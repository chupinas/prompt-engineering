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
                invocationContext
        ).block();
        history.addAll(response);

        return Map.of("input: ", input,
                "response: ", convertChatMessagesToJson(response));
    }

    private String convertChatMessagesToJson(List<ChatMessageContent<?>> messages) {
        try {
            return objectMapper.writeValueAsString(messages.stream().map(msg -> msg.getContent()).toList());
        } catch (Exception e) {
            throw new RuntimeException("Error converting messages to JSON", e);
        }
    }
}
