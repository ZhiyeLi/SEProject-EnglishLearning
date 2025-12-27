-- database_schema_v3.sql
-- 适配 CET4/6 及 TOEFL/IELTS 全题型架构
-- [修改说明] 针对前端“单题模式”、“收藏功能”、“错题统计”进行了结构优化

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =======================================================
-- 1. 清理阶段
-- =======================================================

DROP TABLE IF EXISTS `user_favorites`;      -- [新增] 清理收藏表
DROP TABLE IF EXISTS `user_answer_details`;
DROP TABLE IF EXISTS `user_exam_records`;
DROP TABLE IF EXISTS `user_vocabulary`;
DROP TABLE IF EXISTS `user_question_records`;
DROP TABLE IF EXISTS `question_sub_items`;
DROP TABLE IF EXISTS `questions`;
DROP TABLE IF EXISTS `exam_papers`;

-- =======================================================
-- 2. 重建阶段
-- =======================================================

-- -------------------------------------------------------
-- 表1: 试卷表 
-- -------------------------------------------------------
CREATE TABLE `exam_papers` (
  `id` BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  `name` VARCHAR(255) NOT NULL,
  `category` VARCHAR(32) NOT NULL COMMENT '分类：CET4, CET6, TOEFL, IELTS, KY',
  `year` INT COMMENT '年份',
  `difficulty` TINYINT DEFAULT 3,
  `status` TINYINT DEFAULT 1,
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  INDEX `idx_category` (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -------------------------------------------------------
-- 表2: 题库主表 
-- -------------------------------------------------------
CREATE TABLE `questions` (
  `id` BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  `paper_id` BIGINT UNSIGNED NOT NULL,
  
  `section_type` ENUM('listening', 'reading', 'writing', 'speaking') NOT NULL,
  `section_name` VARCHAR(100),
  
  `title` VARCHAR(255),
  `material_text` LONGTEXT,
  `material_image` VARCHAR(500),
  
  `audio_url` VARCHAR(500),
  `audio_start_sec` INT DEFAULT 0,
  `audio_end_sec` INT DEFAULT 0,
  
  `sort_order` INT DEFAULT 0,
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  
  FOREIGN KEY (`paper_id`) REFERENCES `exam_papers`(`id`) ON DELETE CASCADE,
  INDEX `idx_paper_section` (`paper_id`, `section_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -------------------------------------------------------
-- 表3: 具体小题表
-- -------------------------------------------------------
CREATE TABLE `question_sub_items` (
  `id` BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  `parent_question_id` BIGINT UNSIGNED NOT NULL,
  
  `content` TEXT,
  `item_type` VARCHAR(32) DEFAULT 'choice',
  
  `options` JSON,
  `answer` JSON,
  `explanation` TEXT,
  
  `score_value` DECIMAL(4,1) DEFAULT 1.0,
  `sort_order` INT DEFAULT 0,
  
  FOREIGN KEY (`parent_question_id`) REFERENCES `questions`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -------------------------------------------------------
-- 表4: 用户收藏表 [新增]
-- 作用：支持前端“筛选状态 -> 已收藏”
-- -------------------------------------------------------
CREATE TABLE `user_favorites` (
  `id` BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  `user_id` BIGINT UNSIGNED NOT NULL,
  `question_id` BIGINT UNSIGNED NOT NULL COMMENT '收藏通常是针对大题(篇章)',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  
  UNIQUE KEY `uk_user_question` (`user_id`, `question_id`), -- 防止重复收藏
  FOREIGN KEY (`question_id`) REFERENCES `questions`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -------------------------------------------------------
-- 表5: 用户考试记录 [修改]
-- 修改点：增加 mode 和 target_question_id 以支持“单题模式”
-- -------------------------------------------------------
CREATE TABLE `user_exam_records` (
  `id` BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  `user_id` BIGINT UNSIGNED NOT NULL,
  `paper_id` BIGINT UNSIGNED NOT NULL,
  
  -- [新增] 模式区分：整卷模考 vs 单部分/单题练习
  `mode` ENUM('full_paper', 'single_part') DEFAULT 'full_paper',
  
  -- [新增] 如果是单题模式，记录具体练习的是哪个大题(questions.id)
  `target_question_id` BIGINT UNSIGNED DEFAULT NULL,
  
  `status` ENUM('ongoing', 'completed', 'graded') DEFAULT 'ongoing',
  `total_score` DECIMAL(5,2) DEFAULT 0.00,
  `started_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `completed_at` TIMESTAMP NULL,
  
  INDEX `idx_user_paper` (`user_id`, `paper_id`),
  INDEX `idx_user_mode` (`user_id`, `mode`) -- [新增] 方便查询单题记录
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -------------------------------------------------------
-- 表6: 用户具体答题详情 [修改]
-- 修改点：冗余 user_id 并加索引，优化错题本和统计查询速度
-- -------------------------------------------------------
CREATE TABLE `user_answer_details` (
  `id` BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  `record_id` BIGINT UNSIGNED NOT NULL,
  `sub_item_id` BIGINT UNSIGNED NOT NULL,
  
  -- [新增] 冗余字段，方便不查 record 表直接统计正确率和查错题
  `user_id` BIGINT UNSIGNED NOT NULL,
  
  `user_content` TEXT,
  `is_correct` TINYINT(1) DEFAULT 0,
  `score_obtained` DECIMAL(4,1) DEFAULT 0.0,
  
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  
  INDEX `idx_record` (`record_id`),
  
  -- [新增] 核心统计索引：用于“错题窗口(is_correct=0)”和“今日做题(created_at)”
  INDEX `idx_user_stats` (`user_id`, `is_correct`, `created_at`) 
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SET FOREIGN_KEY_CHECKS = 1;