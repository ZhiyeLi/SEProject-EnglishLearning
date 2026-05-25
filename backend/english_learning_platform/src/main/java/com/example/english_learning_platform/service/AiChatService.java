package com.example.english_learning_platform.service;

import com.example.english_learning_platform.dto.AiChatMessageDTO;
import com.example.english_learning_platform.dto.AiChatSessionDTO;

import java.util.List;

public interface AiChatService {

    List<AiChatSessionDTO> getUserSessions(Long userId);

    AiChatSessionDTO createSession(Long userId, String title);

    List<AiChatMessageDTO> getSessionMessages(Long userId, Long sessionId);

    AiChatMessageDTO saveMessage(Long userId, Long sessionId, String role, String content);

    AiChatSessionDTO updateSessionTitle(Long userId, Long sessionId, String title);

    void deleteSession(Long userId, Long sessionId);
}
