package com.example.english_learning_platform.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;  // 登录用户名

    @Column(nullable = false)
    private String password;  // 加密存储的密码

    private String avatar;  // 头像URL
    private String email;
    private String phone;
    private String signature;  // 个性签名
    private String joinTime;  // 注册时间（格式：yyyy-MM-dd）
    private String location;  // 所在地
}