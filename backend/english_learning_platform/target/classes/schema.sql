-- MySQL 数据库结构
-- 生成时间: 2025-12-11 19:42:54

SET FOREIGN_KEY_CHECKS=0;

-- 表: users
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
  `user_id` BIGINT AUTO_INCREMENT,
  `user_name` VARCHAR(255) NOT NULL,
  `user_password` VARCHAR(255) NOT NULL,
  `user_email` VARCHAR(255) NOT NULL,
  `avatar` VARCHAR(255) DEFAULT 'https://picsum.photos/seed/default/100/100',
  `user_status` VARCHAR(255) DEFAULT '沉迷学习',
  `signature` VARCHAR(255) DEFAULT '这个人很懒，什么都没写',
  `streak` BIGINT DEFAULT '0',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 表: plans
DROP TABLE IF EXISTS `plans`;
CREATE TABLE `plans` (
  `id` BIGINT AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `date` DATE NOT NULL,
  `title` VARCHAR(255) NOT NULL,
  `description` VARCHAR(500),
  `category` VARCHAR(255) NOT NULL DEFAULT '其他',
  `priority` VARCHAR(255) NOT NULL DEFAULT 'medium',
  `start_time` VARCHAR(255),
  `end_time` VARCHAR(255),
  `if_completed` TINYINT(1) NOT NULL DEFAULT '0',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 表: friends
DROP TABLE IF EXISTS `friends`;
CREATE TABLE `friends` (
  `relation_id` BIGINT AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `friend_id` BIGINT NOT NULL,
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`relation_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 表: messages
DROP TABLE IF EXISTS `messages`;
CREATE TABLE `messages` (
  `message_id` BIGINT AUTO_INCREMENT,
  `sender_id` BIGINT NOT NULL,
  `receiver_id` BIGINT NOT NULL,
  `content` VARCHAR(500) NOT NULL,
  `if_read` TINYINT(1) DEFAULT '0',
  `sent_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`message_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 表: friend_requests
DROP TABLE IF EXISTS `friend_requests`;
CREATE TABLE `friend_requests` (
  `request_id` BIGINT AUTO_INCREMENT,
  `sender_id` BIGINT NOT NULL,
  `receiver_id` BIGINT NOT NULL,
  `status` VARCHAR(255) DEFAULT 'pending',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`request_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 表: word_types
DROP TABLE IF EXISTS `word_types`;
CREATE TABLE `word_types` (
  `type_id` BIGINT AUTO_INCREMENT,
  `name` VARCHAR(255) NOT NULL,
  `description` VARCHAR(500),
  `total_words` BIGINT DEFAULT '0',
  PRIMARY KEY (`type_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 表: words
DROP TABLE IF EXISTS `words`;
CREATE TABLE `words` (
  `word_id` BIGINT AUTO_INCREMENT,
  `word` VARCHAR(255) NOT NULL,
  `part_of_speech` VARCHAR(255),
  `phonetic` VARCHAR(255),
  `definition` VARCHAR(500),
  `example` VARCHAR(500),
  `type_id` BIGINT NOT NULL,
  `synonyms` VARCHAR(500),
  `antonyms` VARCHAR(500),
  `usage_notes` VARCHAR(500),
  `audio_url` VARCHAR(255),
  `image_url` VARCHAR(255),
  PRIMARY KEY (`word_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 表: user_word_progress
DROP TABLE IF EXISTS `user_word_progress`;
CREATE TABLE `user_word_progress` (
  `progress_id` BIGINT AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `word_id` BIGINT NOT NULL,
  `type_id` BIGINT NOT NULL,
  `stage` BIGINT DEFAULT '0',
  `last_review_time` TIMESTAMP,
  `next_review_time` TIMESTAMP,
  `review_count` BIGINT DEFAULT '0',
  `passed_date` DATE,
  PRIMARY KEY (`progress_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 表: daily_study_record
DROP TABLE IF EXISTS `daily_study_record`;
CREATE TABLE `daily_study_record` (
  `record_id` BIGINT AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `study_date` DATE NOT NULL,
  `new_words` BIGINT DEFAULT '0',
  `review_words` BIGINT DEFAULT '0',
  `total_words` BIGINT DEFAULT '0',
  `streak` BIGINT DEFAULT '0',
  `type_id` BIGINT,
  PRIMARY KEY (`record_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 表: checkin_plans
DROP TABLE IF EXISTS `checkin_plans`;
CREATE TABLE `checkin_plans` (
  `plan_id` BIGINT AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `type_id` BIGINT NOT NULL,
  `words_per_day` BIGINT NOT NULL,
  `start_date` DATE NOT NULL,
  `status` VARCHAR(255) DEFAULT 'active',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`plan_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 表: user_selected_types
DROP TABLE IF EXISTS `user_selected_types`;
CREATE TABLE `user_selected_types` (
  `id` BIGINT AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `type_id` BIGINT NOT NULL,
  `selected_date` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 表: courses
DROP TABLE IF EXISTS `courses`;
CREATE TABLE `courses` (
  `id` BIGINT AUTO_INCREMENT,
  `name` VARCHAR(255) NOT NULL,
  `description` VARCHAR(500),
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 表: questions
DROP TABLE IF EXISTS `questions`;
CREATE TABLE `questions` (
  `id` BIGINT AUTO_INCREMENT,
  `type` VARCHAR(255) NOT NULL,
  `difficulty` VARCHAR(255) NOT NULL,
  `title` VARCHAR(255) NOT NULL,
  `preview` VARCHAR(500),
  `content` VARCHAR(500) NOT NULL,
  `audio_url` VARCHAR(255),
  `tags` VARCHAR(500),
  `related_course_id` VARCHAR(255),
  `related_chapter` VARCHAR(255),
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 表: question_items
DROP TABLE IF EXISTS `question_items`;
CREATE TABLE `question_items` (
  `id` BIGINT AUTO_INCREMENT,
  `question_id` VARCHAR(255) NOT NULL,
  `question_text` VARCHAR(500) NOT NULL,
  `question_type` VARCHAR(255) NOT NULL,
  `options` VARCHAR(500),
  `answer` VARCHAR(255) NOT NULL,
  `explanation` VARCHAR(500),
  `order_num` BIGINT NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 表: user_question_records
DROP TABLE IF EXISTS `user_question_records`;
CREATE TABLE `user_question_records` (
  `id` BIGINT AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `question_id` VARCHAR(255) NOT NULL,
  `status` VARCHAR(255) DEFAULT 'not-done',
  `last_result` VARCHAR(255),
  `last_attempt_date` TIMESTAMP,
  `correct_count` BIGINT DEFAULT '0',
  `wrong_count` BIGINT DEFAULT '0',
  `is_favorited` TINYINT(1) DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 表: user_answer_details
DROP TABLE IF EXISTS `user_answer_details`;
CREATE TABLE `user_answer_details` (
  `id` BIGINT AUTO_INCREMENT,
  `record_id` VARCHAR(255) NOT NULL,
  `question_item_id` VARCHAR(255) NOT NULL,
  `user_answer` VARCHAR(500) NOT NULL,
  `is_correct` TINYINT(1) NOT NULL,
  `time_spent` BIGINT,
  `answered_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 表: user_vocabulary
DROP TABLE IF EXISTS `user_vocabulary`;
CREATE TABLE `user_vocabulary` (
  `id` BIGINT AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `word_id` BIGINT NOT NULL,
  `translation` VARCHAR(255),
  `source_question_id` VARCHAR(255),
  `if_mastered` TINYINT(1) DEFAULT '0',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 表: user_settings
DROP TABLE IF EXISTS `user_settings`;
CREATE TABLE `user_settings` (
  `id` BIGINT AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `daily_goal` INT DEFAULT '50',
  `vocabulary_difficulty` VARCHAR(255) DEFAULT '四级词汇',
  `review_strategy` VARCHAR(255) DEFAULT '标准模式（1,3,7,15,30天）',
  `remind_time` VARCHAR(10) DEFAULT '08:00',
  `check_in_reminder` TINYINT(1) DEFAULT '1',
  `suggestions_reminder` TINYINT(1) DEFAULT '1',
  `message_reminder` TINYINT(1) DEFAULT '1',
  `dark_mode` TINYINT(1) DEFAULT '0',
  `font_size` VARCHAR(50) DEFAULT 'normal',
  `language` VARCHAR(50) DEFAULT '中文简体',
  `share_score` TINYINT(1) DEFAULT '1',
  `profile_visibility` VARCHAR(255) DEFAULT '公开',
  `friend_request_mode` VARCHAR(255) DEFAULT '所有人',
  `options` JSON DEFAULT NULL COMMENT '所有可用的选项列表（包含词汇难度、复习策略等）',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 表: ai_chat_sessions
DROP TABLE IF EXISTS `ai_chat_sessions`;
CREATE TABLE `ai_chat_sessions` (
  `session_id` BIGINT AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `title` VARCHAR(255),
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`session_id`),
  FOREIGN KEY (`user_id`) REFERENCES `users`(`user_id`) ON DELETE CASCADE,
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_updated_at` (`updated_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 表: ai_chat_messages
DROP TABLE IF EXISTS `ai_chat_messages`;
CREATE TABLE `ai_chat_messages` (
  `message_id` BIGINT AUTO_INCREMENT,
  `session_id` BIGINT NOT NULL,
  `role` VARCHAR(50) NOT NULL COMMENT 'user或assistant',
  `content` LONGTEXT NOT NULL,
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`message_id`),
  FOREIGN KEY (`session_id`) REFERENCES `ai_chat_sessions`(`session_id`) ON DELETE CASCADE,
  INDEX `idx_session_id` (`session_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SET FOREIGN_KEY_CHECKS=1;