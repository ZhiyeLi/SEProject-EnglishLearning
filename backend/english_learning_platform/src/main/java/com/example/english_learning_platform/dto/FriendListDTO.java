package com.example.english_learning_platform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FriendListDTO {
    private String id;      // 转为String防止前端精度丢失，对应前端 ID
    private String name;    // 对应 user_name
    private String avatar;  // 对应 avatar
    private String status;  // 对应 user_status (前端根据这个显示绿点)
    private String signature; // 额外信息
}