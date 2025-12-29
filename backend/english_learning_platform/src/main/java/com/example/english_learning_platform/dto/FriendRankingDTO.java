package com.example.english_learning_platform.dto;
import lombok.Data;

@Data
public class FriendRankingDTO {
    private Long userId;       // 好友ID
    private String avatar;     // 好友头像
    private String username;   // 好友昵称
    private Integer totalWords;// 本周学习单词总数（去重）
}
