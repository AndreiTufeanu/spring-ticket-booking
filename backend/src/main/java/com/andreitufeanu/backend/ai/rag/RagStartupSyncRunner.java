package com.andreitufeanu.backend.ai.rag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RagStartupSyncRunner implements ApplicationRunner {

    private final VectorStore vectorStore;
    private final EventRagService eventRagService;

    @Override
    public void run(@NonNull ApplicationArguments args) {
        if (isEmpty()) {
            log.info("No {} documents found in the vector store; reindexing from the database.", RagDocumentType.EVENT);
            eventRagService.reindexAllEvents();
        }
    }

    private boolean isEmpty() {
        var probe = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(RagDocumentType.EVENT.name())
                        .topK(1)
                        .filterExpression("type == '" + RagDocumentType.EVENT.name() + "'")
                        .build()
        );
        return probe.isEmpty();
    }
}