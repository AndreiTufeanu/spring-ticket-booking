package com.andreitufeanu.backend.chat.service;

import com.andreitufeanu.backend.chat.dto.ChatMessageDto;
import com.andreitufeanu.backend.chat.entity.ChatMessage;
import com.andreitufeanu.backend.chat.enums.ChatMessageRole;
import com.andreitufeanu.backend.chat.mapper.ChatMessageMapper;
import com.andreitufeanu.backend.chat.repository.ChatMessageRepository;
import com.andreitufeanu.backend.exceptions.NotFoundException;
import com.andreitufeanu.backend.user.entity.User;
import com.andreitufeanu.backend.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatService {

    private final ChatClient chatClient;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final ChatMessageMapper chatMessageMapper;

    @Transactional
    public ChatMessageDto sendMessage(UUID userId, String message) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));

        Instant sentAt = Instant.now();
        String response;
        try {
            response = chatClient.prompt()
                    .user(message)
                    .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, userId.toString()))
                    .call()
                    .content();
        } finally {
            chatMessageRepository.save(new ChatMessage(user, ChatMessageRole.USER, message, sentAt));
        }

        ChatMessage assistantMessage = chatMessageRepository.save(
                new ChatMessage(user, ChatMessageRole.ASSISTANT, response, Instant.now())
        );

        return chatMessageMapper.toResponse(assistantMessage);
    }

    public List<ChatMessageDto> getMessages(UUID userId) {
        return chatMessageRepository
                .findAllByUserIdOrderByCreatedAtAsc(userId)
                .stream()
                .map(chatMessageMapper::toResponse)
                .toList();
    }
}