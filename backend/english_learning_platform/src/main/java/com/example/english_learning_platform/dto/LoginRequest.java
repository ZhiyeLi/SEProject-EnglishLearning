package com.example.english_learning_platform.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String userName;
    private String userPassword;
}
