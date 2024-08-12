package com.keiken.chatGPT_API.service;

public interface ChatService {
    String getChatGptResponse(String token, String question, String username);
    String continueConversation(Long conversationId, String token, String question);
}
