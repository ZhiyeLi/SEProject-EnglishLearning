package com.example.english_learning_platform.repository;

import com.example.english_learning_platform.entity.AiChatSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AiChatSessionRepository extends JpaRepository<AiChatSession, Long> {
    List<AiChatSession> findByUserIdOrderByUpdatedAtDesc(Long userId);
    AiChatSession findBySessionIdAndUserId(Long sessionId, Long userId);
}
