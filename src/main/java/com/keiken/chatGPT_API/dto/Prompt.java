package com.keiken.chatGPT_API.dto;

import lombok.Data;

@Data
public class Prompt {
    private String token;
    private String question;
    private String username;

}
