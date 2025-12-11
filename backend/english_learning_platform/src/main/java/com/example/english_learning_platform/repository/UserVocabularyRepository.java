package com.example.english_learning_platform.repository;

import com.example.english_learning_platform.entity.UserVocabulary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserVocabularyRepository extends JpaRepository<UserVocabulary, Long> {
    List<UserVocabulary> findByUserId(Long userId);
    Optional<UserVocabulary> findByUserIdAndWordId(Long userId, Long wordId);
    List<UserVocabulary> findByUserIdAndIfMastered(Long userId, Boolean ifMastered);
}
