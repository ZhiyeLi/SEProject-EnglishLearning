package com.example.english_learning_platform.repository;

import com.example.english_learning_platform.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    
    @Query("SELECT m FROM Message m WHERE (m.senderId = ?1 AND m.receiverId = ?2) OR (m.senderId = ?2 AND m.receiverId = ?1) ORDER BY m.sentAt DESC")
    List<Message> findConversation(Long userId1, Long userId2);
    
    Long countByReceiverIdAndIfRead(Long receiverId, Boolean ifRead);
    
    List<Message> findByReceiverIdOrderBySentAtDesc(Long receiverId);
}
