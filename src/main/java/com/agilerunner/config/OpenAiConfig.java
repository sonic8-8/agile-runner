package com.agilerunner.config;

import com.agilerunner.domain.exception.AgileRunnerException;
import com.agilerunner.domain.exception.ErrorCode;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.model.SimpleApiKey;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Configuration
public class OpenAiConfig {

    @Bean
    @ConditionalOnProperty(prefix = "spring.ai.openai.chat", name = "enabled", havingValue = "true")
    public ChatClient chatClient(@Value("${spring.ai.openai.api-key:}") String apiKey,
                                 @Value("${spring.ai.openai.base-url:https://api.openai.com}") String baseUrl,
                                 @Value("${spring.ai.openai.chat.model:gpt-4o-mini}") String model) {
        if (!StringUtils.hasText(apiKey)) {
            throw new AgileRunnerException(
                    ErrorCode.OPENAI_CLIENT_MISSING,
                    "OpenAI API Key가 설정되지 않았습니다."
            );
        }

        OpenAiApi openAiApi = OpenAiApi.builder()
                .baseUrl(baseUrl)
                .apiKey(new SimpleApiKey(apiKey))
                .build();

        OpenAiChatModel chatModel = OpenAiChatModel.builder()
                .openAiApi(openAiApi)
                .defaultOptions(OpenAiChatOptions.builder().model(model).build())
                .build();

        return ChatClient.builder(chatModel).build();
    }
}
