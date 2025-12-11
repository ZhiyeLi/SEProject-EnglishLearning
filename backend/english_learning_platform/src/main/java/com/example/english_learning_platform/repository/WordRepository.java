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
     * 根据类型ID查找单词列表
     */
    List<Word> findByTypeId(Long typeId);
    
    /**
     * 根据类型ID分页查找单词
     */
    List<Word> findByTypeIdOrderByWordIdAsc(Long typeId);
}
