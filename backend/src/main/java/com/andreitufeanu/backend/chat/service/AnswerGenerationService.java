package com.andreitufeanu.backend.chat.service;

import com.andreitufeanu.backend.ai.factcheck.FactCheckService;
import com.andreitufeanu.backend.ai.tools.BookingTools;
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

import java.util.ArrayList;
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
     * Generates an answer and verifies it against everything the model had access to:
     * the retrieved RAG context and the outputs of any tools it called.
     *
     * <p>Tool outputs are collected through a per-attempt list passed in the tool
     * context (see {@link BookingTools#TOOL_OUTPUTS_KEY}). If bespoke-minicheck rejects
     * the answer, this throws and Spring Retry re-runs the whole thing (a fresh model
     * call, fresh retrieval, fresh tool calls, fresh check) up to {@code maxAttempts}
     * times.
     *
     * @param userId  the user the answer is for; also used as the chat memory conversation ID
     * @param message the user's message
     * @return a grounded answer
     * @throws InvalidAnswerException if the answer is judged ungrounded (retried, then
     *                                handled by {@link #recover})
     */
    @Retryable(retryFor = InvalidAnswerException.class, maxAttempts = 3)
    public String generateAnswer(UUID userId, String message) {
        List<String> toolOutputs = new ArrayList<>();

        ChatResponse chatResponse = chatClient.prompt()
                .user(message)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, userId.toString()))
                .toolContext(Map.of(
                        "userId", userId.toString(),
                        BookingTools.TOOL_OUTPUTS_KEY, toolOutputs))
                .call()
                .chatResponse();

        String answer = chatResponse.getResult().getOutput().getText();
        List<Document> retrievedDocuments =
                chatResponse.getMetadata().get(QuestionAnswerAdvisor.RETRIEVED_DOCUMENTS);

        log.info("Tool outputs recorded: {}", toolOutputs);

        if (!factCheckService.isGrounded(answer, retrievedDocuments, toolOutputs)) {
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