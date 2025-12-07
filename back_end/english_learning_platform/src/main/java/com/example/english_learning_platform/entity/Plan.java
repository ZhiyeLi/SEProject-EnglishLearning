package com.example.english_learning_platform.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.Date;

@Data
@Entity
@Table(name = "plans")
public class Plan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;  // 关联用户ID（外键）

    private String title;  // 计划标题
    private String category;  // 分类（词汇、听力等）
    private Date date;  // 计划日期
    private Boolean completed = false;  // 是否完成
    private String note;  // 备注
    private String priority;  // 优先级（high/medium/low）
}