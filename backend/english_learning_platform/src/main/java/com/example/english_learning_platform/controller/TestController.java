package com.example.english_learning_platform.controller;

import com.example.english_learning_platform.entity.Word;
import com.example.english_learning_platform.entity.WordType;
import com.example.english_learning_platform.repository.UserRepository;
import com.example.english_learning_platform.repository.WordRepository;
import com.example.english_learning_platform.repository.WordTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 测试控制器
 * 用于验证数据库连接和基本功能
 */
@RestController
@RequestMapping("/api/test")
@CrossOrigin(origins = "*")
public class TestController {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private WordRepository wordRepository;
    
    @Autowired
    private WordTypeRepository wordTypeRepository;
    
    /**
     * 健康检查
     */
    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "UP");
        result.put("message", "MySQL 后端服务运行正常");
        result.put("timestamp", System.currentTimeMillis());
        return result;
    }
    
    /**
     * 获取数据统计
     */
    @GetMapping("/stats")
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("userCount", userRepository.count());
        stats.put("wordCount", wordRepository.count());
        stats.put("wordTypeCount", wordTypeRepository.count());
        return stats;
    }
    
    /**
     * 获取所有单词类型
     */
    @GetMapping("/word-types")
    public List<WordType> getWordTypes() {
        return wordTypeRepository.findAll();
    }
    
    /**
     * 获取所有单词
     */
    @GetMapping("/words")
    public List<Word> getWords() {
        return wordRepository.findAll();
    }
    
    /**
     * 根据类型获取单词
     */
    @GetMapping("/words/type/{typeId}")
    public List<Word> getWordsByType(@PathVariable Long typeId) {
        return wordRepository.findByTypeId(typeId);
    }
}
