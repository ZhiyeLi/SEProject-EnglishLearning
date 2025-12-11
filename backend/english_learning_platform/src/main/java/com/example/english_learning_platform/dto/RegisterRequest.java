package com.example.english_learning_platform.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String userName;
    private String userPassword;
    private String userEmail;
}
