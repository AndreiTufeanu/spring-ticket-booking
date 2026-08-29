package com.andreitufeanu.backend.event.service;

import com.andreitufeanu.backend.ai.rag.RagDocumentType;
import com.andreitufeanu.backend.event.entity.Event;
import com.andreitufeanu.backend.event.mapper.EventDocumentMapper;
import com.andreitufeanu.backend.event.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class EventRagService {

    private final VectorStore vectorStore;
    private final EventDocumentMapper eventDocumentMapper;
    private final EventRepository eventRepository;

    public void indexEvent(Event event) {
        vectorStore.add(List.of(eventDocumentMapper.toDocument(event)));
    }

    public void updateEvent(Event event) {
        indexEvent(event);
    }

    public void deleteEvent(UUID eventId) {
        vectorStore.delete(List.of(eventId.toString()));
    }

    public void reindexAllEvents() {
        vectorStore.delete("type == '" + RagDocumentType.EVENT.name() + "'");

        List<Document> documents = eventRepository.findAllForReindexing()
                .stream()
                .map(eventDocumentMapper::toDocument)
                .toList();

        if (!documents.isEmpty()) {
            vectorStore.add(documents);
        }
    }
}