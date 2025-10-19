package com.example.VerduleriaBot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class BotConfig {

    @Value("${telegram.bot.token}")
    private String botToken;

    @Bean
    public String telegramBotToken() {
        return botToken;
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}