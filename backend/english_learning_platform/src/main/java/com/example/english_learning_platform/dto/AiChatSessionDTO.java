package com.example.english_learning_platform.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiChatSessionDTO {
    private Long sessionId;
    private String title;
    private Long messageCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
