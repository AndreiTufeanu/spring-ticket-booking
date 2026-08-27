package com.andreitufeanu.backend.ai.rag;

import com.andreitufeanu.backend.event.entity.Category;
import com.andreitufeanu.backend.event.entity.Event;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.stream.Collectors;

@Component
public class EventDocumentMapper {

    public Document toDocument(Event event) {

        String categories = event.getCategories()
                .stream()
                .map(Category::getName)
                .collect(Collectors.joining(", "));

        String content = """
                [ID: %s]
                Event: %s
                Description: %s
                Location: %s
                Date: %s
                Categories: %s
                Total Seats: %s
                Available Seats: %s
                """.formatted(
                event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getLocation(),
                event.getEventDate(),
                categories,
                event.getTotalSeats(),
                event.getAvailableSeats()
        );

        return new Document(
                event.getId().toString(),
                content,
                Map.of(
                        "type", RagDocumentType.EVENT.name(),
                        "entityId", event.getId().toString()
                )
        );
    }
}