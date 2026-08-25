package com.andreitufeanu.backend.chat.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatClient chatClient;

    @GetMapping
    public String getMessage(@RequestParam("message") String message) {
        return chatClient.prompt()
                .user(message)
                .call()
                .content();
    }

}
