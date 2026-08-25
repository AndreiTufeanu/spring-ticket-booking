package com.andreitufeanu.backend.chat.repository;

import com.andreitufeanu.backend.chat.entity.ChatMessage;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface ChatMessageRepository extends CrudRepository<ChatMessage, UUID> {

    List<ChatMessage> findAllByUserIdOrderByCreatedAtAsc(UUID userId);
    List<ChatMessage> findTop10ByUserIdOrderByCreatedAtDesc(UUID userId);
}
