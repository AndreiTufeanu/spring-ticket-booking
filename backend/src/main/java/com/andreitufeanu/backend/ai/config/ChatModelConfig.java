package com.andreitufeanu.backend.ai.config;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class ChatModelConfig {

    @Bean
    @Primary
    public ChatModel reasoningChatModel(OllamaApi ollamaApi) {
        return OllamaChatModel.builder()
                .ollamaApi(ollamaApi)
                .options(
                        OllamaChatOptions.builder()
                                .model("qwen3:8b")
                                .temperature(0.4)
                                .build()
                )
                .build();
    }
}
