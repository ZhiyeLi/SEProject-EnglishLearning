package com.example.english_learning_platform.repository;

import com.example.english_learning_platform.entity.AiChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AiChatMessageRepository extends JpaRepository<AiChatMessage, Long> {
    List<AiChatMessage> findBySessionIdOrderByCreatedAtAsc(Long sessionId);

    @Query("SELECT m.sessionId, COUNT(m) FROM AiChatMessage m WHERE m.sessionId IN :sessionIds GROUP BY m.sessionId")
    List<Object[]> countMessagesBySessionIds(@Param("sessionIds") List<Long> sessionIds);
}
