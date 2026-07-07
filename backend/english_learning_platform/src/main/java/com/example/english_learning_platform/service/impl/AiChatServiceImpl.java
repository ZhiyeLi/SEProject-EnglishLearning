package com.example.english_learning_platform.service.impl;

import com.example.english_learning_platform.dto.AiChatSessionDTO;
import com.example.english_learning_platform.dto.AiChatMessageDTO;
import com.example.english_learning_platform.entity.AiChatSession;
import com.example.english_learning_platform.entity.AiChatMessage;
import com.example.english_learning_platform.repository.AiChatSessionRepository;
import com.example.english_learning_platform.repository.AiChatMessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.english_learning_platform.service.AiChatService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AiChatServiceImpl implements AiChatService {

    private final AiChatSessionRepository sessionRepository;
    private final AiChatMessageRepository messageRepository;

    public AiChatServiceImpl(AiChatSessionRepository sessionRepository,
                        AiChatMessageRepository messageRepository) {
        this.sessionRepository = sessionRepository;
        this.messageRepository = messageRepository;
    }

    /**
     * 获取用户的所有对话会话列表（按最后更新时间倒序）
     */
    public List<AiChatSessionDTO> getUserSessions(Long userId) {
        List<AiChatSession> sessions = sessionRepository.findByUserIdOrderByUpdatedAtDesc(userId);

        List<Long> sessionIds = sessions.stream()
                .map(AiChatSession::getSessionId)
                .collect(Collectors.toList());

        Map<Long, Long> countMap = Collections.emptyMap();
        if (!sessionIds.isEmpty()) {
            countMap = messageRepository.countMessagesBySessionIds(sessionIds)
                    .stream()
                    .collect(Collectors.toMap(
                            row -> (Long) row[0],
                            row -> (Long) row[1]
                    ));
        }

        final Map<Long, Long> finalCountMap = countMap;
        return sessions.stream()
                .map(session -> convertToDTO(session, finalCountMap.getOrDefault(session.getSessionId(), 0L)))
                .collect(Collectors.toList());
    }

    /**
     * 创建新的对话会话
     */
    @Transactional
    public AiChatSessionDTO createSession(Long userId, String title) {
        AiChatSession session = new AiChatSession();
        session.setUserId(userId);
        session.setTitle(title);
        session.setCreatedAt(LocalDateTime.now());
        session.setUpdatedAt(LocalDateTime.now());

        AiChatSession savedSession = sessionRepository.save(session);
        return convertToDTO(savedSession, 0L);
    }

    /**
     * 获取指定对话会话的所有消息
     */
    public List<AiChatMessageDTO> getSessionMessages(Long userId, Long sessionId) {
        AiChatSession session = sessionRepository.findBySessionIdAndUserId(sessionId, userId);
        if (session == null) {
            throw new RuntimeException("对话会话不存在或无权限访问");
        }

        List<AiChatMessage> messages = messageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
        return messages.stream()
                .map(this::convertMessageToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 保存一条消息到指定的对话会话
     */
    @Transactional
    public AiChatMessageDTO saveMessage(Long userId, Long sessionId, String role, String content) {
        AiChatSession session = sessionRepository.findBySessionIdAndUserId(sessionId, userId);
        if (session == null) {
            throw new RuntimeException("对话会话不存在或无权限访问");
        }

        AiChatMessage message = new AiChatMessage();
        message.setSessionId(sessionId);
        message.setRole(role);
        message.setContent(content);
        message.setCreatedAt(LocalDateTime.now());

        AiChatMessage savedMessage = messageRepository.save(message);

        // 更新会话的 updated_at 时间
        session.setUpdatedAt(LocalDateTime.now());
        sessionRepository.save(session);

        return convertMessageToDTO(savedMessage);
    }

    /**
     * 更新对话会话标题
     */
    @Transactional
    public AiChatSessionDTO updateSessionTitle(Long userId, Long sessionId, String title) {
        AiChatSession session = sessionRepository.findBySessionIdAndUserId(sessionId, userId);
        if (session == null) {
            throw new RuntimeException("对话会话不存在或无权限访问");
        }

        session.setTitle(title);
        session.setUpdatedAt(LocalDateTime.now());
        AiChatSession updatedSession = sessionRepository.save(session);

        long messageCount = messageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId).size();
        return convertToDTO(updatedSession, messageCount);
    }

    /**
     * 删除对话会话及其所有消息
     */
    @Transactional
    public void deleteSession(Long userId, Long sessionId) {
        AiChatSession session = sessionRepository.findBySessionIdAndUserId(sessionId, userId);
        if (session == null) {
            throw new RuntimeException("对话会话不存在或无权限访问");
        }

        // 级联删除该会话的所有消息
        List<AiChatMessage> messages = messageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
        messageRepository.deleteAll(messages);

        // 删除会话
        sessionRepository.delete(session);
    }

    /**
     * 将AiChatSession实体转换为DTO
     */
    private AiChatSessionDTO convertToDTO(AiChatSession session, long messageCount) {
        AiChatSessionDTO dto = new AiChatSessionDTO();
        dto.setSessionId(session.getSessionId());
        dto.setTitle(session.getTitle());
        dto.setMessageCount(messageCount);
        dto.setCreatedAt(session.getCreatedAt());
        dto.setUpdatedAt(session.getUpdatedAt());
        return dto;
    }

    /**
     * 将AiChatMessage实体转换为DTO
     */
    private AiChatMessageDTO convertMessageToDTO(AiChatMessage message) {
        AiChatMessageDTO dto = new AiChatMessageDTO();
        dto.setMessageId(message.getMessageId());
        dto.setSessionId(message.getSessionId());
        dto.setRole(message.getRole());
        dto.setContent(message.getContent());
        dto.setCreatedAt(message.getCreatedAt());

        return dto;
    }
}
