package com.keiken.chatGPT_API.service;

import com.keiken.chatGPT_API.model.Conversation;

import java.util.List;

public interface ConversationService {
    List<Conversation> getConversationsByUsername(String username);
    Conversation getConversationById(Long conversationId);
}
