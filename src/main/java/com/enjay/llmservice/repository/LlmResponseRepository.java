package com.enjay.llmservice.repository;

import com.enjay.llmservice.entity.LlmResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LlmResponseRepository extends JpaRepository<LlmResponse, Long> {

    /**
     * Find all responses for a specific conversation
     * @param conversationId the ID of the conversation
     * @return list of responses for the conversation
     */
    List<LlmResponse> findByConversationIdOrderByTimestampDesc(String conversationId);

    /**
     * Find all responses for a specific conversation and response type
     * @param conversationId the ID of the conversation
     * @param responseType the type of response (e.g., ORIGINAL, EN, KO, CN, JP)
     * @return list of responses for the conversation and response type
     */
    List<LlmResponse> findByConversationIdAndResponseTypeOrderByTimestampDesc(String conversationId, LlmResponse.ResponseType responseType);

    /**
     * Find the most recent response for a specific conversation and response type
     * @param conversationId the ID of the conversation
     * @param responseType the type of response (e.g., ORIGINAL, EN, KO, CN, JP)
     * @return the most recent response for the conversation and response type
     */
    Optional<LlmResponse> findFirstByConversationIdAndResponseTypeOrderByTimestampDesc(String conversationId, LlmResponse.ResponseType responseType);
}
