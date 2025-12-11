package com.example.english_learning_platform.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long userId;
    private String userName;
    private String userEmail;
    private String avatar;
    private String userStatus;
    private String signature;
    private Integer streak;
    private String createdAt;
}
