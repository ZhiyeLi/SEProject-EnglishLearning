package com.example.english_learning_platform.dto;

import lombok.Data;

@Data
public class UserUpdateDTO {
    private String avatar;     // 头像URL
    private String signature;  // 个性签名
    private String location;   // 所在地
    private String email;      // 邮箱
    private String phone;      // 手机号
}