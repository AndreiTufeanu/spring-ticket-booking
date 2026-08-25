package com.andreitufeanu.backend.chat.controller;

import com.andreitufeanu.backend.chat.dto.ChatMessageDto;
import com.andreitufeanu.backend.chat.dto.ChatRequest;
import com.andreitufeanu.backend.chat.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping
    public ChatMessageDto sendMessage(@Valid @RequestBody ChatRequest request, Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        return chatService.sendMessage(userId, request.message());
    }

    @GetMapping("/messages")
    public List<ChatMessageDto> getMessages(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        return chatService.getMessages(userId);
    }
}
