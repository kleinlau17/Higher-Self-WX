package com.tencent.wxcloudrun.config;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.time.Duration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@Configuration
@EnableConfigurationProperties(DeepseekChatModelProperties.class)
public class AiConfig {
    
    @Bean
    public ChatLanguageModel chatLanguageModel(DeepseekChatModelProperties properties) {
        return OpenAiChatModel.builder()
                .apiKey(properties.getApiKey())
                .baseUrl(properties.getBaseUrl())
                .modelName(properties.getModelName())
                .timeout(Duration.ofSeconds(90))
                .maxRetries(3)
                .logRequests(true)
                .logResponses(true)
                .build();
    }
} 