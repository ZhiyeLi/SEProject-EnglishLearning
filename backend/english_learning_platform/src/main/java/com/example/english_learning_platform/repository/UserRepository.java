package com.example.english_learning_platform.repository;

import com.example.english_learning_platform.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 用户数据访问接口
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * 根据用户名查找用户
     */
    Optional<User> findByUserName(String userName);
    
    /**
     * 根据邮箱查找用户
     */
    Optional<User> findByUserEmail(String userEmail);
    
    /**
     * 检查用户名是否存在
     */
    boolean existsByUserName(String userName);
    
    /**
     * 检查邮箱是否存在
     */
    boolean existsByUserEmail(String userEmail);
}
