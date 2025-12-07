package com.example.english_learning_platform.vo;

import com.example.english_learning_platform.entity.User;
import lombok.Data;

@Data
public class LoginVO {
    private String token;
    private User userInfo;  // 前端需要的用户信息
}