package com.keiken.chatGPT_API.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.keiken.chatGPT_API.model.Chat;
import com.keiken.chatGPT_API.model.Conversation;
import com.keiken.chatGPT_API.model.User;
import com.keiken.chatGPT_API.repository.ChatRepository;
import com.keiken.chatGPT_API.repository.ConversationRepository;
import com.keiken.chatGPT_API.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class ChatServiceImpl implements ChatService{
    private final ConversationRepository conversationRepository;
    private final ChatRepository chatRepository;
    private final UserRepository userRepository;
    private final RestTemplate restTemplate;

    @Autowired
    public ChatServiceImpl(ConversationRepository conversationRepository,
                           UserRepository userRepository,
                           RestTemplate restTemplate,
                           ChatRepository chatRepository) {
        this.conversationRepository = conversationRepository;
        this.userRepository = userRepository;
        this.restTemplate = restTemplate;
        this.chatRepository = chatRepository;
    }

    @Override
    public String getChatGptResponse(String token, String question, String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            user = new User();
            user.setUsername(username);
            userRepository.save(user);
        }

        // Find the latest conversation or create a new one
        Conversation conversation = conversationRepository.findTopByUserOrderByIdDesc(user);
        if (conversation == null) {
            conversation = new Conversation();
            conversation.setTitle(generateTitleFromQuestion(question));
            conversation.setUser(user);
            conversationRepository.save(conversation);
        }

        // Fetch chat history for the conversation
        List<Chat> chatHistory = chatRepository.findByConversation(conversation);

        // Prepare the messages to send to ChatGPT
        List<Map<String, String>> messages = new ArrayList<>();
        for (Chat chat : chatHistory) {
            messages.add(new HashMap<>() {{
                put("role", "user");
                put("content", chat.getQuestion());
            }});
            messages.add(new HashMap<>() {{
                put("role", "assistant");
                put("content", chat.getResponse());
            }});
        }

        // Add the new question
        messages.add(new HashMap<>() {{
            put("role", "user");
            put("content", question);
        }});

        String apiUrl = "https://api.openai.com/v1/chat/completions";
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-3.5-turbo");
        requestBody.put("messages", messages);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> responseEntity = restTemplate.postForEntity(apiUrl, requestEntity, String.class);

        String response = extractResponseContent(responseEntity.getBody());

        // Save the new chat
        Chat chat = new Chat();
        chat.setConversation(conversation);
        chat.setQuestion(question);
        chat.setResponse(response);

        chatRepository.save(chat);

        return response;
    }

    @Override
    public String continueConversation(Long conversationId, String token, String question) {
        Conversation conversation = conversationRepository.findById(conversationId).orElse(null);
        if (conversation == null) {
            throw new RuntimeException("Conversation not found");
        }

        if (conversation.getTitle() == null || conversation.getTitle().isEmpty()) {
            conversation.setTitle(generateTitleFromQuestion(question));
            conversationRepository.save(conversation);
        }

        List<Chat> chatHistory = chatRepository.findByConversation(conversation);

        // Prepare the messages to send to ChatGPT
        List<Map<String, String>> messages = new ArrayList<>();
        for (Chat chat : chatHistory) {
            messages.add(new HashMap<>() {{
                put("role", "user");
                put("content", chat.getQuestion());
            }});
            messages.add(new HashMap<>() {{
                put("role", "assistant");
                put("content", chat.getResponse());
            }});
        }

        // Add the new question
        messages.add(new HashMap<>() {{
            put("role", "user");
            put("content", question);
        }});

        String apiUrl = "https://api.openai.com/v1/chat/completions";
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-3.5-turbo");
        requestBody.put("messages", messages);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> responseEntity = restTemplate.postForEntity(apiUrl, requestEntity, String.class);

        String response = extractResponseContent(responseEntity.getBody());

        // Save the new chat
        Chat chat = new Chat();
        chat.setConversation(conversation);
        chat.setQuestion(question);
        chat.setResponse(response);

        chatRepository.save(chat);

        return response;
    }


    private String extractResponseContent(String response) {
        String content = "";
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.readTree(response);
            content = root.path("choices").get(0).path("message").path("content").asText();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return content;
    }
    private String generateTitleFromQuestion(String question) {
        String[] title = question.split(" ");
        return String.join(" ", Arrays.copyOf(title, Math.min(title.length, 3))) + "...";
    }
}
