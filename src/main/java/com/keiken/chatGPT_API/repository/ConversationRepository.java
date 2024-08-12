package com.keiken.chatGPT_API.repository;

import com.keiken.chatGPT_API.model.Chat;
import com.keiken.chatGPT_API.model.Conversation;
import com.keiken.chatGPT_API.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    List<Conversation> findByUser(User user);

    Conversation findTopByUserOrderByIdDesc(User user);
}