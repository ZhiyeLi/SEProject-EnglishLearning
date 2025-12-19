package com.example.english_learning_platform.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiChatMessageDTO {
    private Long messageId;
    private Long sessionId;
    private String role;
    private String content;
    private LocalDateTime createdAt;
}
