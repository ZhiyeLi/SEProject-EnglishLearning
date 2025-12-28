package com.example.english_learning_platform.repository;

import com.example.english_learning_platform.entity.UserWordProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserWordProgressRepository extends JpaRepository<UserWordProgress, Long> {
    List<UserWordProgress> findByUserIdAndTypeId(Long userId, Long typeId);
    Optional<UserWordProgress> findByUserIdAndWordId(Long userId, Long wordId);
    Long countByUserIdAndPassedDateIsNotNull(Long userId);
    Long countByUserIdAndTypeIdAndPassedDateIsNotNull(Long userId, Long typeId);
    List<UserWordProgress> findByUserIdAndPassedDateIsNotNull(Long userId);
    List<UserWordProgress> findByUserIdAndPassedDate(Long userId, LocalDate passedDate);
    List<UserWordProgress> findByUserIdAndTypeIdAndPassedDateIsNotNull(Long userId, Long typeId);

    /**
     * 获取用户已掌握的去重后的单词总数
     */
    @org.springframework.data.jpa.repository.Query(value = "SELECT COUNT(*) FROM (SELECT 1 FROM user_word_progress up JOIN words w ON up.word_id = w.word_id WHERE up.user_id = ?1 AND up.passed_date IS NOT NULL GROUP BY w.word, w.part_of_speech) as t", nativeQuery = true)
    long countUniquePassedWordsByUserId(Long userId);

    /**
     * 获取用户在特定类型下已掌握的去重后的单词总数
     */
    @org.springframework.data.jpa.repository.Query(value = "SELECT COUNT(*) FROM (SELECT 1 FROM user_word_progress up JOIN words w ON up.word_id = w.word_id WHERE up.user_id = ?1 AND up.type_id = ?2 AND up.passed_date IS NOT NULL GROUP BY w.word, w.part_of_speech) as t", nativeQuery = true)
    long countUniquePassedWordsByUserIdAndTypeId(Long userId, Long typeId);

    /**
     * 获取用户已掌握的单词唯一标识集合 (word|part_of_speech)
     */
    @org.springframework.data.jpa.repository.Query(value = "SELECT DISTINCT CONCAT(w.word, '|', w.part_of_speech) FROM user_word_progress up JOIN words w ON up.word_id = w.word_id WHERE up.user_id = ?1 AND up.type_id = ?2 AND up.passed_date IS NOT NULL", nativeQuery = true)
    List<String> findPassedWordKeysByUserIdAndTypeId(Long userId, Long typeId);
}
