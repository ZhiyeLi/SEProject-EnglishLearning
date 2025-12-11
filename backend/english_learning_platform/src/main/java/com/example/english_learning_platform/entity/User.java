package com.example.english_learning_platform.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 匹配本地表的username字段（唯一、非空）
    @Column(name = "username", unique = true, nullable = false, length = 50)
    private String username;

    @Column(name = "password", nullable = false, length = 100)
    private String password;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "avatar", length = 255)
    private String avatar;

    @Column(name = "signature", length = 255)
    private String signature;

    @Column(name = "user_status", length = 255)
    private String userStatus;

    @Column(name = "location", length = 100)
    private String location;

    @Column(name = "join_time")
    private LocalDateTime joinTime;

    @PrePersist
    protected void onCreate() {
        joinTime = LocalDateTime.now();
    }
}