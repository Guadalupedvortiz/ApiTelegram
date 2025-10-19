package com.example.VerduleriaBot.controller;

import com.example.VerduleriaBot.model.TelegramUpdate;
import com.example.VerduleriaBot.service.TelegramService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/webhook")
@RequiredArgsConstructor
public class TelegramController {

    private final TelegramService telegramService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping
    public ResponseEntity<String> handleWebhook(@RequestBody TelegramUpdate update) {
        try {
            telegramService.processUpdate(update);
            return ResponseEntity.ok("{\"status\":\"ok\"}");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("{\"status\":\"error\"}");
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("{\"status\":\"running\", \"service\":\"VerduleriaBot\"}");
    }
}