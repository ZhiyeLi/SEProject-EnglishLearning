package com.example.english_learning_platform.repository;

import com.example.english_learning_platform.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    // 根据用户名查询
    Optional<User> findByUsername(String username);

    // 根据手机号查询
    Optional<User> findByPhone(String phone);

    // 根据邮箱查询
    Optional<User> findByEmail(String email);
}