package com.example.english_learning_platform.repository;

import com.example.english_learning_platform.entity.UserQuestionRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserQuestionRecordRepository extends JpaRepository<UserQuestionRecord, Long> {
    Optional<UserQuestionRecord> findByUserIdAndQuestionId(Long userId, String questionId);
    List<UserQuestionRecord> findByUserId(Long userId);
    List<UserQuestionRecord> findByUserIdAndStatus(Long userId, String status);
    Long countByUserIdAndStatus(Long userId, String status);
    List<UserQuestionRecord> findByUserIdAndIsFavorited(Long userId, Boolean isFavorited);
}
