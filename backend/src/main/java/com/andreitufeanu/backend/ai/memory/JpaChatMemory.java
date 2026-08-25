package com.andreitufeanu.backend.ai.memory;

import com.andreitufeanu.backend.chat.entity.ChatMessage;
import com.andreitufeanu.backend.chat.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Adapts the application's persisted chat messages to Spring AI's {@link ChatMemory} contract.
 *
 * <p>The application's {@code chat_messages} table remains the source of truth for
 * conversation history.
 */
@Component
@RequiredArgsConstructor
public class JpaChatMemory implements ChatMemory {

    private final ChatMessageRepository chatMessageRepository;

    @Override
    public void add(@NonNull String conversationId, @NonNull List<Message> messages) {
        // No-op: ChatService already persists both messages.
        // Writing here would create a second write path to the same table.
    }

    @Override
    public List<Message> get(@NonNull String conversationId) {
        UUID userId = UUID.fromString(conversationId);

        List<ChatMessage> lastMessages =
                chatMessageRepository.findTop10ByUserIdOrderByCreatedAtDesc(userId);

        Collections.reverse(lastMessages);

        return lastMessages.stream()
                .map(JpaChatMemory::toSpringAiMessage)
                .toList();
    }


    /**
     * Chat history deletion is handled explicitly through the repository rather than
     * through Spring AI's memory abstraction.
     *
     * @throws UnsupportedOperationException always
     */
    @Override
    public void clear(@NonNull String conversationId) {
        throw new UnsupportedOperationException("Use ChatMessageRepository to delete history instead.");
    }

    private static Message toSpringAiMessage(ChatMessage message) {
        return switch (message.getRole()) {
            case USER -> new UserMessage(message.getContent());
            case ASSISTANT -> new AssistantMessage(message.getContent());
        };
    }
}