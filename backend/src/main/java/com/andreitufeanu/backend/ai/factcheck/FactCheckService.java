package com.andreitufeanu.backend.ai.factcheck;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.evaluation.FactCheckingEvaluator;
import org.springframework.ai.document.Document;
import org.springframework.ai.evaluation.EvaluationRequest;
import org.springframework.ai.evaluation.EvaluationResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Verifies that a generated answer is actually supported by the context it was
 * generated from, using bespoke-minicheck as the judge.
 *
 * <p>The context has two sources: the RAG documents retrieved for the turn, and
 * the outputs of any tools the main model invoked. Tool calls run inside the chat
 * model and never appear in the advisor chain's retrieved documents, so they are
 * passed in separately.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FactCheckService {

    private final FactCheckingEvaluator factCheckingEvaluator;

    /**
     * Checks whether an answer is supported by the RAG documents and tool outputs
     * produced during the same turn.
     *
     * <p>Each tool output is wrapped in its own {@link Document} and appended to the
     * retrieved documents, so the checker sees everything the main model saw.
     *
     * @param answer             the assistant's generated answer (the "claim")
     * @param retrievedDocuments the RAG documents retrieved for that turn; may be {@code null}
     * @param toolOutputs        the recorded results of tool calls made during that turn;
     *                           may be {@code null} or empty
     * @return {@code true} if bespoke-minicheck considers the answer grounded, or if
     *         there is no context at all to check it against (no retrieved documents
     *         and no tool calls)
     */
    public boolean isGrounded(String answer, List<Document> retrievedDocuments, List<String> toolOutputs) {
        List<Document> context = new ArrayList<>();
        if (retrievedDocuments != null) {
            context.addAll(retrievedDocuments);
        }
        if (toolOutputs != null) {
            toolOutputs.stream().map(Document::new).forEach(context::add);
        }

        if (context.isEmpty()) {
            return true;
        }

        EvaluationResponse evaluation = factCheckingEvaluator.evaluate(new EvaluationRequest(context, answer));

        if (!evaluation.isPass()) {
            log.warn("bespoke-minicheck flagged a possibly ungrounded answer: \"{}\"", answer);
        }
        return evaluation.isPass();
    }
}