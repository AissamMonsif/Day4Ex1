package com.keiken.chatGPT_API.contoller;

import com.keiken.chatGPT_API.dto.Prompt;
import com.keiken.chatGPT_API.model.Conversation;
import com.keiken.chatGPT_API.service.ChatService;
import com.keiken.chatGPT_API.service.ConversationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@Api(value = "Chat Controller", description = "APIs for sending prompts and managing conversations with ChatGPT")
public class ChatController {

    private final ChatService chatService;
    private final ConversationService conversationService;

    @Autowired
    public ChatController(ChatService chatService, ConversationService conversationService) {
        this.chatService = chatService;
        this.conversationService = conversationService;
    }

    @PostMapping("/prompts")
    @ApiOperation(value = "Send a prompt to ChatGPT and receive a response", response = String.class)
    public String getResponse(@RequestBody Prompt prompt) {
        return chatService.getChatGptResponse(prompt.getToken(), prompt.getQuestion(), prompt.getUsername());
    }

    @GetMapping("/conversations")
    @ApiOperation(value = "List all conversations for a specific user", response = Conversation.class)
    public List<Conversation> getConversations(@RequestParam String username) {
        return conversationService.getConversationsByUsername(username);
    }

    @GetMapping("/conversations/{conversationId}")
    @ApiOperation(value = "Get a conversation by its ID", response = Conversation.class)
    public Conversation getConversationById(@PathVariable Long conversationId) {
        return conversationService.getConversationById(conversationId);
    }

    @PostMapping("/conversations/{conversationId}/continue")
    @ApiOperation(value = "Continue a previous conversation using its ID", response = String.class)
    public String continueConversation(@PathVariable Long conversationId, @RequestBody Prompt prompt) {
        return chatService.continueConversation(conversationId, prompt.getToken(), prompt.getQuestion());
    }
}
