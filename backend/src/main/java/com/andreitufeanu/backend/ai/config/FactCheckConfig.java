package com.andreitufeanu.backend.ai.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.evaluation.FactCheckingEvaluator;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Configuration
public class FactCheckConfig {

    @Value("classpath:/prompts/fact-check.st")
    private Resource factCheckPrompt;

    /**
     * A second, independent ChatModel pointed at bespoke-minicheck.
     *
     * Deliberately NOT @Primary - reasoningChatModel stays the default for everything else.
     * This bean is only ever wired in below, by qualifier.
     */
    @Bean
    public ChatModel factCheckChatModel(OllamaApi ollamaApi) {
        return OllamaChatModel.builder()
                .ollamaApi(ollamaApi)
                .options(
                        OllamaChatOptions.builder()
                                .model("bespoke-minicheck:7b")
                                .temperature(0.0)
                                .numPredict(2)
                                .build()
                )
                .build();
    }

    @Bean
    public FactCheckingEvaluator factCheckingEvaluator(
            @Qualifier("factCheckChatModel") ChatModel factCheckChatModel) throws IOException {
        String evaluationPrompt = StreamUtils.copyToString(
                factCheckPrompt.getInputStream(), StandardCharsets.UTF_8);

        return FactCheckingEvaluator.builder(ChatClient.builder(factCheckChatModel))
                .evaluationPrompt(evaluationPrompt)
                .build();
    }
}