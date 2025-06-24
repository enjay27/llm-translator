package com.enjay.llmservice;

import com.enjay.llmservice.entity.LlmResponse;
import com.enjay.llmservice.repository.LlmResponseRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class Controller {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final ChatMemory chatMemory;
    private final ChatMemoryRepository chatMemoryRepository;
    private final OllamaChatModel ollamaChatModel;
    private final LlmResponseRepository llmResponseRepository;

    @GetMapping("/llm/{id}")
    public ResponseEntity<?> getLlmResponse(@PathVariable String id) {
        return ResponseEntity.ok(chatMemoryRepository.findByConversationId(id));
    }

    @PostMapping("/llm/{id}")
    public ResponseEntity<?> generateLlmResponse(@PathVariable String id, @RequestBody MessageRequest messageRequest) {
        Message systemMessage = new SystemMessage("""
                You should respond translation of user-messageRequest in Korean, English, Japanese and Chinese. response will be JSON format.
                example format
                {
                    "en": "ENGLISH CONTEXT",
                    "ko": "KOREAN CONTEXT",
                    "cn": "CHINESE CONTEXT",
                    "jp": "JAPANESE CONTEXT"
                }
                """);
        Message userMessage = new UserMessage(messageRequest.text());
        Prompt prompt = new Prompt(systemMessage, userMessage);
        ChatResponse call = ollamaChatModel.call(prompt);

        // Store in chat memory
        chatMemory.add(id, userMessage);
        chatMemory.add(id, call.getResult().getOutput());

        // Store in database
        String responseContent = call.getResult().getOutput().toString();

        // Save the original response first
        LlmResponse originalResponse = LlmResponse.builder()
                .conversationId(id)
                .userMessage(messageRequest.text())
                .llmResponse(responseContent)
                .responseType(LlmResponse.ResponseType.ORIGINAL)
                .build();
        llmResponseRepository.save(originalResponse);

        // Try to parse JSON response and extract language-specific content
        try {
            JsonNode jsonNode = objectMapper.readTree(responseContent);

            // Extract and save language-specific responses if available
            if (jsonNode.has("en")) {
                LlmResponse englishResponse = LlmResponse.builder()
                        .conversationId(id)
                        .userMessage(messageRequest.text())
                        .llmResponse(jsonNode.get("en").asText())
                        .responseType(LlmResponse.ResponseType.EN)
                        .build();
                llmResponseRepository.save(englishResponse);
            }

            if (jsonNode.has("ko")) {
                LlmResponse koreanResponse = LlmResponse.builder()
                        .conversationId(id)
                        .userMessage(messageRequest.text())
                        .llmResponse(jsonNode.get("ko").asText())
                        .responseType(LlmResponse.ResponseType.KO)
                        .build();
                llmResponseRepository.save(koreanResponse);
            }

            if (jsonNode.has("cn")) {
                LlmResponse chineseResponse = LlmResponse.builder()
                        .conversationId(id)
                        .userMessage(messageRequest.text())
                        .llmResponse(jsonNode.get("cn").asText())
                        .responseType(LlmResponse.ResponseType.CN)
                        .build();
                llmResponseRepository.save(chineseResponse);
            }

            if (jsonNode.has("jp")) {
                LlmResponse japaneseResponse = LlmResponse.builder()
                        .conversationId(id)
                        .userMessage(messageRequest.text())
                        .llmResponse(jsonNode.get("jp").asText())
                        .responseType(LlmResponse.ResponseType.JP)
                        .build();
                llmResponseRepository.save(japaneseResponse);
            }
        } catch (JsonProcessingException e) {
            log.warn("Failed to parse JSON response: {}", e.getMessage());
            // Already saved the raw response, so no additional action needed
        }

        return ResponseEntity.ok(call);
    }

    public record MessageRequest(String text) {}
}
