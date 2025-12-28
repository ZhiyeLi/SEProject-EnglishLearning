package com.example.english_learning_platform.repository;

import com.example.english_learning_platform.entity.Word;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 单词数据访问接口
 */
@Repository
public interface WordRepository extends JpaRepository<Word, Long> {
    
    /**
     * 根据单词查找
     */
    Optional<Word> findByWord(String word);

    /**
     * 根据单词和词性查找
     */
    List<Word> findByWordAndPartOfSpeech(String word, String partOfSpeech);
    
    /**
     * 根据类型ID查找单词列表
     */
    List<Word> findByTypeId(Long typeId);
    
    /**
     * 根据类型ID分页查找单词
     */
    List<Word> findByTypeIdOrderByWordIdAsc(Long typeId);

    /**
     * 获取去重后的单词总数
     */
    @org.springframework.data.jpa.repository.Query(value = "SELECT COUNT(*) FROM (SELECT 1 FROM words WHERE type_id = ?1 GROUP BY word, part_of_speech) as t", nativeQuery = true)
    long countUniqueWordsByTypeId(Long typeId);

    /**
     * 获取合并后的单词列表，并按词性排序（名词、动词、形容词、副词）及字母序
     */
    @org.springframework.data.jpa.repository.Query(value = "SELECT MIN(word_id) as word_id, word, part_of_speech, MAX(phonetic) as phonetic, " +
            "GROUP_CONCAT(definition SEPARATOR '；') as definition, " +
            "GROUP_CONCAT(example SEPARATOR ' | ') as example, " +
            "type_id, MAX(synonyms) as synonyms, MAX(antonyms) as antonyms, " +
            "MAX(usage_notes) as usage_notes, MAX(audio_url) as audio_url, MAX(image_url) as imageUrl " +
            "FROM words WHERE type_id = ?1 GROUP BY word, part_of_speech " +
            "ORDER BY (CASE part_of_speech " +
            "  WHEN 'noun' THEN 1 " +
            "  WHEN 'verb' THEN 2 " +
            "  WHEN 'adjective' THEN 3 " +
            "  WHEN 'adverb' THEN 4 " +
            "  ELSE 5 END), word ASC", nativeQuery = true)
    List<Object[]> findMergedWordsByTypeId(Long typeId);

    /**
     * 获取用户已掌握的合并后的单词列表，并按词性排序及字母序
     */
    @org.springframework.data.jpa.repository.Query(value = "SELECT MIN(w.word_id) as word_id, w.word, w.part_of_speech, MAX(w.phonetic) as phonetic, " +
            "GROUP_CONCAT(w.definition SEPARATOR '；') as definition, " +
            "GROUP_CONCAT(w.example SEPARATOR ' | ') as example, " +
            "w.type_id, MAX(w.synonyms) as synonyms, MAX(w.antonyms) as antonyms, " +
            "MAX(w.usage_notes) as usage_notes, MAX(w.audio_url) as audio_url, MAX(w.image_url) as imageUrl " +
            "FROM words w JOIN user_word_progress up ON w.word_id = up.word_id " +
            "WHERE up.user_id = ?1 AND up.passed_date IS NOT NULL " +
            "GROUP BY w.word, w.part_of_speech " +
            "ORDER BY (CASE w.part_of_speech " +
            "  WHEN 'noun' THEN 1 " +
            "  WHEN 'verb' THEN 2 " +
            "  WHEN 'adjective' THEN 3 " +
            "  WHEN 'adverb' THEN 4 " +
            "  ELSE 5 END), w.word ASC", nativeQuery = true)
    List<Object[]> findMergedPassedWordsByUserId(Long userId);
}
