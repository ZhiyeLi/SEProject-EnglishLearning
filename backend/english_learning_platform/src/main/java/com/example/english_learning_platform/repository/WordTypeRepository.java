package com.example.english_learning_platform.repository;

import com.example.english_learning_platform.entity.WordType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 单词类型数据访问接口
 */
@Repository
public interface WordTypeRepository extends JpaRepository<WordType, Long> {
    
    /**
     * 根据名称查找单词类型
     */
    Optional<WordType> findByName(String name);
}
