package com.keiken.chatGPT_API.service;

import com.keiken.chatGPT_API.model.Conversation;
import com.keiken.chatGPT_API.model.User;
import com.keiken.chatGPT_API.repository.ConversationRepository;
import com.keiken.chatGPT_API.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConversationServiceImpl implements ConversationService {

    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;

    @Autowired
    public ConversationServiceImpl(ConversationRepository conversationRepository, UserRepository userRepository) {
        this.conversationRepository = conversationRepository;
        this.userRepository = userRepository;
    }

    @Override
    public List<Conversation> getConversationsByUsername(String username) {
        User user = userRepository.findByUsername(username);
        if (user != null) {
            return conversationRepository.findByUser(user);
        }
        return List.of();
    }

    @Override
    public Conversation getConversationById(Long conversationId) {
        return conversationRepository.findById(conversationId).orElse(null);
    }
}
