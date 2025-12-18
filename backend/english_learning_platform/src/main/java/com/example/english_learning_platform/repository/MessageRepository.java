package com.example.english_learning_platform.repository;

import com.example.english_learning_platform.dto.UnreadCountDTO;
import com.example.english_learning_platform.entity.Message;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    
    @Query("SELECT m FROM Message m WHERE (m.senderId = ?1 AND m.receiverId = ?2) OR (m.senderId = ?2 AND m.receiverId = ?1) ORDER BY m.sentAt DESC")
    List<Message> findConversation(Long userId1, Long userId2);
    
    Long countByReceiverIdAndIfRead(Long receiverId, Boolean ifRead);
    
    List<Message> findByReceiverIdOrderBySentAtDesc(Long receiverId);

    // 按sender_id分组统计未读消息数
    @Query("SELECT new com.example.english_learning_platform.dto.UnreadCountDTO(m.senderId, COUNT(m)) " +
            "FROM Message m " +
            "WHERE m.receiverId = :userId AND m.ifRead = false " +
            "GROUP BY m.senderId")
    List<UnreadCountDTO> countUnreadByReceiverIdGroupBySenderId(@Param("userId") Long userId);

    // 标记消息为已读的更新方法
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE Message m SET m.ifRead = :ifRead WHERE m.receiverId = :receiverId AND m.senderId = :senderId AND m.ifRead = false")
    void updateIfReadByReceiverIdAndSenderId(
            @Param("receiverId") Long receiverId,
            @Param("senderId") Long senderId,
            @Param("ifRead") Boolean ifRead
    );
}
