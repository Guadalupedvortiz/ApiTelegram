package com.example.VerduleriaBot.model;


import lombok.Data;

@Data
public class Message {
    private Long message_id;
    private Chat chat;
    private String text;
}
