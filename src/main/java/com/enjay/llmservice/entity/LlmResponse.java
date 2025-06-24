package com.enjay.llmservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "llm_responses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LlmResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String conversationId;

    @Column(nullable = false)
    private String userMessage;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String llmResponse;

    /**
     * Type discriminator for language responses
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ResponseType responseType;

    /**
     * Enum representing the type of response
     */
    public enum ResponseType {
        ORIGINAL, EN, KO, CN, JP
    }

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @PrePersist
    protected void onCreate() {
        timestamp = LocalDateTime.now();
    }
}
