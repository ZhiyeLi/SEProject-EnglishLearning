package com.example.english_learning_platform.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UnreadCountDTO {
    // 好友ID（即消息的发送者ID sender_id）
    private Long friendId;
    // 该好友的未读消息数
    private Long count;
}