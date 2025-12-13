package com.example.english_learning_platform.repository;

import com.example.english_learning_platform.entity.UserSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 用户设置数据访问接口
 */
@Repository
public interface UserSettingsRepository extends JpaRepository<UserSettings, Long> {
    
    /**
     * 根据用户ID查找用户设置
     */
    Optional<UserSettings> findByUserId(Long userId);
    
    /**
     * 检查用户设置是否存在
     */
    boolean existsByUserId(Long userId);
}
