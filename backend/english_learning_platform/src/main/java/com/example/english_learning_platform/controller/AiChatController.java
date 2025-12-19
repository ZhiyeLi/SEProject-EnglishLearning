package com.example.english_learning_platform.controller;

import com.example.english_learning_platform.dto.ApiResponse;
import com.example.english_learning_platform.dto.AiChatSessionDTO;
import com.example.english_learning_platform.dto.AiChatMessageDTO;
import com.example.english_learning_platform.service.AiChatService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai-chat")
public class AiChatController {
    
    private final AiChatService aiChatService;
    
    public AiChatController(AiChatService aiChatService) {
        this.aiChatService = aiChatService;
    }
    
    /**
     * 获取用户的所有对话会话列表
     */
    @GetMapping("/sessions")
    public ApiResponse<List<AiChatSessionDTO>> getUserSessions(
            @RequestAttribute("userId") Long userId) {
        try {
            List<AiChatSessionDTO> sessions = aiChatService.getUserSessions(userId);
            return ApiResponse.success(sessions);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 创建新的对话会话
     */
    @PostMapping("/sessions")
    public ApiResponse<AiChatSessionDTO> createSession(
            @RequestAttribute("userId") Long userId,
            @RequestBody(required = false) Map<String, String> body) {
        try {
            String title = body != null ? body.get("title") : null;
            AiChatSessionDTO session = aiChatService.createSession(userId, title);
            return ApiResponse.success(session);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 获取指定对话会话的所有消息
     */
    @GetMapping("/sessions/{sessionId}/messages")
    public ApiResponse<List<AiChatMessageDTO>> getSessionMessages(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long sessionId) {
        try {
            List<AiChatMessageDTO> messages = aiChatService.getSessionMessages(userId, sessionId);
            return ApiResponse.success(messages);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 保存一条消息到指定的对话会话
     */
    @PostMapping("/sessions/{sessionId}/messages")
    public ApiResponse<AiChatMessageDTO> saveMessage(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long sessionId,
            @RequestBody Map<String, String> body) {
        try {
            String role = body.get("role");
            String content = body.get("content");
            AiChatMessageDTO message = aiChatService.saveMessage(userId, sessionId, role, content);
            return ApiResponse.success(message);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 更新对话会话标题
     */
    @PutMapping("/sessions/{sessionId}")
    public ApiResponse<AiChatSessionDTO> updateSessionTitle(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long sessionId,
            @RequestBody Map<String, String> body) {
        try {
            String title = body.get("title");
            AiChatSessionDTO session = aiChatService.updateSessionTitle(userId, sessionId, title);
            return ApiResponse.success(session);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 删除对话会话
     */
    @DeleteMapping("/sessions/{sessionId}")
    public ApiResponse<String> deleteSession(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long sessionId) {
        try {
            aiChatService.deleteSession(userId, sessionId);
            return ApiResponse.success("对话已删除");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
