package com.andreitufeanu.backend.chat.service;

import com.andreitufeanu.backend.ai.factcheck.FactCheckService;
import com.andreitufeanu.backend.exceptions.InvalidAnswerException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.document.Document;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnswerGenerationService {

    private static final String FALLBACK_ANSWER =
            "I'm sorry, I could not answer your question. Please try rephrasing it.";

    private final ChatClient chatClient;
    private final FactCheckService factCheckService;

    /**
     * Generates an answer and verifies it against the retrieved RAG context.
     * If bespoke-minicheck rejects it, this throws and Spring Retry re-runs
     * the whole thing - a fresh model call, fresh retrieval, fresh check -
     * up to maxAttempts times.
     */
    @Retryable(retryFor = InvalidAnswerException.class, maxAttempts = 3)
    public String generateAnswer(UUID userId, String message) {
        ChatResponse chatResponse = chatClient.prompt()
                .user(message)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, userId.toString()))
                .toolContext(Map.of("userId", userId.toString()))
                .call()
                .chatResponse();

        String answer = chatResponse.getResult().getOutput().getText();
        List<Document> retrievedDocuments =
                chatResponse.getMetadata().get(QuestionAnswerAdvisor.RETRIEVED_DOCUMENTS);

        if (!factCheckService.isGrounded(answer, retrievedDocuments)) {
            throw new InvalidAnswerException("bespoke-minicheck rejected the generated answer as ungrounded");
        }

        return answer;
    }

    @Recover
    public String recover(InvalidAnswerException e, UUID userId, String message) {
        log.warn("Giving up after retries for user {}; falling back to default answer. Reason: {}",
                userId, e.getMessage());
        return FALLBACK_ANSWER;
    }
}