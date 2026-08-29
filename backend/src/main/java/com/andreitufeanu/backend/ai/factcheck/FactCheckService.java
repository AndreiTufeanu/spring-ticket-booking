package com.andreitufeanu.backend.ai.factcheck;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.evaluation.FactCheckingEvaluator;
import org.springframework.ai.document.Document;
import org.springframework.ai.evaluation.EvaluationRequest;
import org.springframework.ai.evaluation.EvaluationResponse;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Verifies that a generated answer is actually supported by the RAG context
 * it was generated from, using bespoke-minicheck as the judge.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FactCheckService {

    private final FactCheckingEvaluator factCheckingEvaluator;

    /**
     * @param answer             the assistant's generated answer (the "claim")
     * @param retrievedDocuments the RAG documents retrieved for that turn (the "document")
     * @return true if bespoke-minicheck considers the answer grounded, or if
     *         there was no retrieved context to check it against at all
     *         (e.g. a purely tool-driven answer with no matching events)
     */
    public boolean isGrounded(String answer, List<Document> retrievedDocuments) {
        if (retrievedDocuments == null || retrievedDocuments.isEmpty()) {
            return true;
        }

        EvaluationResponse evaluation = factCheckingEvaluator.evaluate(
                new EvaluationRequest(retrievedDocuments, answer));

        if (!evaluation.isPass()) {
            log.warn("bespoke-minicheck flagged a possibly ungrounded answer: \"{}\"", answer);
        }

        return evaluation.isPass();
    }
}