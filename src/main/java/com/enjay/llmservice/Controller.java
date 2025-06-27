package com.enjay.llmservice;

import com.enjay.llmservice.entity.LlmResponse;
import com.enjay.llmservice.repository.LlmResponseRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "LLM Translation API", description = "API for translating text using LLM models into multiple languages")
public class Controller {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final ChatMemory chatMemory;
    private final ChatMemoryRepository chatMemoryRepository;
    private final OllamaChatModel ollamaChatModel;
    private final LlmResponseRepository llmResponseRepository;

    @Operation(
        summary = "Get conversation by ID",
        description = "Retrieves the chat memory for a specific conversation ID"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Conversation found",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "Conversation not found"
        )
    })
    @GetMapping("/llm/{id}")
    public ResponseEntity<?> getLlmResponse(
            @Parameter(description = "Conversation ID", required = true) 
            @PathVariable String id) {
        return ResponseEntity.ok(chatMemoryRepository.findByConversationId(id));
    }

    @Operation(
        summary = "Generate translations",
        description = "Translates the provided text into multiple languages using LLM"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Translation successful",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Invalid request"
        ),
        @ApiResponse(
            responseCode = "500", 
            description = "Error during translation"
        )
    })
    @PostMapping("/llm/{id}")
    public ResponseEntity<?> generateLlmResponse(
            @Parameter(description = "Conversation ID", required = true) 
            @PathVariable String id, 
            @Parameter(description = "Message to translate and target languages", required = true)
            @RequestBody MessageRequest messageRequest) {
        // Default to all languages if none specified
        List<String> targetLanguages = messageRequest.targetLanguages();
        if (targetLanguages == null || targetLanguages.isEmpty()) {
            targetLanguages = List.of("en", "ko", "cn", "jp");
        }

        // Build the language part of the prompt
        StringBuilder languagePrompt = new StringBuilder();
        StringBuilder exampleFormat = new StringBuilder("{\n");

        for (String lang : targetLanguages) {
            String languageName;
            switch (lang.toLowerCase()) {
                case "en" -> languageName = "English";
                case "ko" -> languageName = "Korean";
                case "cn" -> languageName = "Chinese";
                case "jp" -> languageName = "Japanese";
                default -> {
                    languageName = lang;
                    log.warn("Unknown language code: {}", lang);
                }
            }

            if (languagePrompt.length() > 0) {
                languagePrompt.append(", ");
            }
            languagePrompt.append(languageName);

            // Add to example format
            exampleFormat.append("    \"").append(lang.toLowerCase()).append("\": \"")
                    .append(languageName.toUpperCase()).append(" CONTEXT\",\n");
        }

        // Remove the last comma and newline
        if (exampleFormat.toString().endsWith(",\n")) {
            exampleFormat.delete(exampleFormat.length() - 2, exampleFormat.length());
        }
        exampleFormat.append("\n}");

        Message systemMessage = new SystemMessage(String.format("""
                You should respond translation of user-messageRequest in %s. response will be JSON format.
                example format
                %s
                """, languagePrompt, exampleFormat));
        Message userMessage = new UserMessage(messageRequest.text());
        Prompt prompt = new Prompt(systemMessage, userMessage);
        ChatResponse call = ollamaChatModel.call(prompt);

        // Store in chat memory
        chatMemory.add(id, userMessage);
        chatMemory.add(id, call.getResult().getOutput());

        // Store in database
        String responseContent = call.getResult().getOutput().getText();

        // Remove <think> tags if present
        responseContent = removeThinkTags(responseContent);

        // Try to parse JSON response and extract language-specific content
        try {
            JsonNode jsonNode = objectMapper.readTree(responseContent);

            // Process only the requested languages
            for (String lang : targetLanguages) {
                String langLower = lang.toLowerCase();
                if (jsonNode.has(langLower)) {
                    LlmResponse.ResponseType responseType;
                    switch (langLower) {
                        case "en" -> responseType = LlmResponse.ResponseType.EN;
                        case "ko" -> responseType = LlmResponse.ResponseType.KO;
                        case "cn" -> responseType = LlmResponse.ResponseType.CN;
                        case "jp" -> responseType = LlmResponse.ResponseType.JP;
                        default -> {
                            log.warn("Unsupported language code for database storage: {}", langLower);
                            continue;
                        }
                    }

                    LlmResponse response = LlmResponse.builder()
                            .conversationId(id)
                            .userMessage(messageRequest.text())
                            .llmResponse(jsonNode.get(langLower).asText())
                            .responseType(responseType)
                            .build();
                    llmResponseRepository.save(response);
                } else {
                    log.warn("Requested language {} not found in LLM response", langLower);
                }
            }
        } catch (JsonProcessingException e) {
            log.warn("Failed to parse JSON response: {}", e.getMessage());
            // Already saved the raw response, so no additional action needed
        }

        return ResponseEntity.ok(call);
    }

    @Schema(description = "Request for translation")
    public record MessageRequest(
            @Schema(description = "Text to translate", example = "Hello, how are you?", required = true)
            String text,

            @Schema(description = "Target languages for translation (en, ko, cn, jp). If empty, all languages will be used.", 
                   example = "[\"en\", \"ko\", \"cn\", \"jp\"]")
            List<String> targetLanguages) {}

    /**
     * Removes <think> tags and their contents from the input string.
     * 
     * @param input The input string that may contain <think> tags
     * @return The input string with <think> tags and their contents removed
     */
    private String removeThinkTags(String input) {
        if (input == null) {
            return null;
        }
        return input.replaceAll("(?s)<think>.*?</think>", "");
    }
}
