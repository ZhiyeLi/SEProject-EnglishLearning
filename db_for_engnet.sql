/*
 Navicat Premium Data Transfer

 Source Server         : dbexp3
 Source Server Type    : MySQL
 Source Server Version : 80018
 Source Host           : localhost:3306
 Source Schema         : db_for_engnet

 Target Server Type    : MySQL
 Target Server Version : 80018
 File Encoding         : 65001

 Date: 11/12/2025 08:45:34
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for book_word_relations
-- ----------------------------
DROP TABLE IF EXISTS `book_word_relations`;
CREATE TABLE `book_word_relations`  (
  `relation_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '关联ID',
  `book_id` int(11) NOT NULL COMMENT '书籍ID',
  `word_id` int(11) NOT NULL COMMENT '单词ID',
  `word_order` int(11) NOT NULL COMMENT '单词顺序',
  PRIMARY KEY (`relation_id`) USING BTREE,
  UNIQUE INDEX `uk_book_word`(`book_id`, `word_id`) USING BTREE,
  INDEX `word_id`(`word_id`) USING BTREE,
  INDEX `idx_book_order`(`book_id`, `word_order`) USING BTREE,
  CONSTRAINT `book_word_relations_ibfk_1` FOREIGN KEY (`book_id`) REFERENCES `books` (`book_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `book_word_relations_ibfk_2` FOREIGN KEY (`word_id`) REFERENCES `words` (`word_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '单词-书籍关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for books
-- ----------------------------
DROP TABLE IF EXISTS `books`;
CREATE TABLE `books`  (
  `book_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '书籍ID',
  `book_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '单词书名称',
  `book_cover` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '书籍封面URL',
  `total_words` int(11) NULL DEFAULT 0 COMMENT '总单词数',
  `type_id` int(11) NOT NULL COMMENT '关联词汇类型ID',
  PRIMARY KEY (`book_id`) USING BTREE,
  INDEX `type_id`(`type_id`) USING BTREE,
  INDEX `idx_name`(`book_name`) USING BTREE,
  CONSTRAINT `books_ibfk_1` FOREIGN KEY (`type_id`) REFERENCES `word_types` (`type_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '单词书籍表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for chat_messages
-- ----------------------------
DROP TABLE IF EXISTS `chat_messages`;
CREATE TABLE `chat_messages`  (
  `message_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `sender_id` int(11) NOT NULL COMMENT '发送者ID',
  `receiver_id` int(11) NOT NULL COMMENT '接收者ID',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '消息内容',
  `message_type` enum('text','image','file') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'text' COMMENT '消息类型',
  `if_read` tinyint(1) NULL DEFAULT 0 COMMENT '是否已读',
  `sent_at` timestamp(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '发送时间',
  PRIMARY KEY (`message_id`) USING BTREE,
  INDEX `idx_sender_receiver`(`sender_id`, `receiver_id`) USING BTREE,
  INDEX `idx_receiver_sender`(`receiver_id`, `sender_id`) USING BTREE,
  INDEX `idx_sent_time`(`sent_at`) USING BTREE,
  CONSTRAINT `chat_messages_ibfk_1` FOREIGN KEY (`sender_id`) REFERENCES `userss` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `chat_messages_ibfk_2` FOREIGN KEY (`receiver_id`) REFERENCES `userss` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '聊天消息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for courses
-- ----------------------------
DROP TABLE IF EXISTS `courses`;
CREATE TABLE `courses`  (
  `course_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '课程UUID',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '课程名称',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '课程描述',
  `created_at` timestamp(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  PRIMARY KEY (`course_id`) USING BTREE,
  INDEX `idx_name`(`name`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '课程表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for daily_study_records
-- ----------------------------
DROP TABLE IF EXISTS `daily_study_records`;
CREATE TABLE `daily_study_records`  (
  `record_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `user_id` int(11) NOT NULL COMMENT '用户ID',
  `type_id` int(11) NOT NULL COMMENT '词汇类型ID',
  `study_date` date NOT NULL COMMENT '学习日期',
  `new_words` int(11) NULL DEFAULT 0 COMMENT '新学单词数',
  `review_words` int(11) NULL DEFAULT 0 COMMENT '复习单词数',
  `total_words` int(11) NULL DEFAULT 0 COMMENT '总学习单词数',
  `created_at` timestamp(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  PRIMARY KEY (`record_id`) USING BTREE,
  UNIQUE INDEX `uk_user_date_type`(`user_id`, `study_date`, `type_id`) USING BTREE,
  INDEX `type_id`(`type_id`) USING BTREE,
  INDEX `idx_user_date`(`user_id`, `study_date`) USING BTREE,
  INDEX `idx_date`(`study_date`) USING BTREE,
  CONSTRAINT `daily_study_records_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `userss` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `daily_study_records_ibfk_2` FOREIGN KEY (`type_id`) REFERENCES `word_types` (`type_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '每日学习记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for friend_relations
-- ----------------------------
DROP TABLE IF EXISTS `friend_relations`;
CREATE TABLE `friend_relations`  (
  `relation_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '关系ID',
  `user_id` int(11) NOT NULL COMMENT '用户ID',
  `friend_id` int(11) NOT NULL COMMENT '好友ID',
  `created_at` timestamp(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '成为好友时间',
  PRIMARY KEY (`relation_id`) USING BTREE,
  UNIQUE INDEX `uk_user_friend`(`user_id`, `friend_id`) USING BTREE,
  INDEX `idx_user`(`user_id`) USING BTREE,
  INDEX `idx_friend`(`friend_id`) USING BTREE,
  CONSTRAINT `friend_relations_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `userss` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `friend_relations_ibfk_2` FOREIGN KEY (`friend_id`) REFERENCES `userss` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '好友关系表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for friend_requests
-- ----------------------------
DROP TABLE IF EXISTS `friend_requests`;
CREATE TABLE `friend_requests`  (
  `request_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '请求ID',
  `sender_id` int(11) NOT NULL COMMENT '发送者ID',
  `receiver_id` int(11) NOT NULL COMMENT '接收者ID',
  `status` enum('pending','accepted','rejected') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'pending' COMMENT '请求状态',
  `created_at` timestamp(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  PRIMARY KEY (`request_id`) USING BTREE,
  INDEX `idx_receiver_status`(`receiver_id`, `status`) USING BTREE,
  INDEX `idx_sender`(`sender_id`) USING BTREE,
  CONSTRAINT `friend_requests_ibfk_1` FOREIGN KEY (`sender_id`) REFERENCES `userss` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `friend_requests_ibfk_2` FOREIGN KEY (`receiver_id`) REFERENCES `userss` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '好友请求表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for plans
-- ----------------------------
DROP TABLE IF EXISTS `plans`;
CREATE TABLE `plans`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `category` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `completed` bit(1) NULL DEFAULT NULL,
  `date` datetime(6) NULL DEFAULT NULL,
  `note` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `priority` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `user_id` bigint(20) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for question_items
-- ----------------------------
DROP TABLE IF EXISTS `question_items`;
CREATE TABLE `question_items`  (
  `item_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '小题UUID',
  `question_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '所属题目ID',
  `question_text` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '小题题干',
  `item_type` enum('choice','essay','fill') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '小题类型',
  `options` json NULL COMMENT '选项数组',
  `answer` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '正确答案',
  `explanation` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '答案解析',
  `order_num` int(11) NOT NULL COMMENT '排序序号',
  PRIMARY KEY (`item_id`) USING BTREE,
  INDEX `idx_question`(`question_id`) USING BTREE,
  INDEX `idx_order`(`order_num`) USING BTREE,
  CONSTRAINT `question_items_ibfk_1` FOREIGN KEY (`question_id`) REFERENCES `questions` (`question_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '小题表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for questions
-- ----------------------------
DROP TABLE IF EXISTS `questions`;
CREATE TABLE `questions`  (
  `question_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '题目UUID',
  `question_type` enum('listening','reading','writing','grammar','vocabulary') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '题型',
  `difficulty` enum('beginner','cet4-6','postgraduate','toefl-ielts','professional') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '难度',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '题目标题',
  `preview` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '题目预览',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '题目内容',
  `audio_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '音频URL',
  `tags` json NULL COMMENT '标签数组',
  PRIMARY KEY (`question_id`) USING BTREE,
  INDEX `idx_type`(`question_type`) USING BTREE,
  INDEX `idx_difficulty`(`difficulty`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '题目表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for study_plans
-- ----------------------------
DROP TABLE IF EXISTS `study_plans`;
CREATE TABLE `study_plans`  (
  `plan_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '计划ID',
  `user_id` int(11) NOT NULL COMMENT '用户ID',
  `date` date NOT NULL COMMENT '计划日期',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '计划标题',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '计划描述',
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '其他' COMMENT '分类',
  `start_time` time(0) NULL DEFAULT NULL COMMENT '开始时间',
  `end_time` time(0) NULL DEFAULT NULL COMMENT '结束时间',
  `if_completed` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否完成',
  `created_at` timestamp(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `updated_at` timestamp(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`plan_id`) USING BTREE,
  UNIQUE INDEX `uk_user_date_title`(`user_id`, `date`, `title`) USING BTREE,
  INDEX `idx_user_date`(`user_id`, `date`) USING BTREE,
  INDEX `idx_user_date_range`(`user_id`, `date`) USING BTREE,
  INDEX `idx_completed`(`if_completed`) USING BTREE,
  CONSTRAINT `study_plans_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `userss` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '学习计划表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for user_answer_details
-- ----------------------------
DROP TABLE IF EXISTS `user_answer_details`;
CREATE TABLE `user_answer_details`  (
  `answer_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '答案记录UUID',
  `record_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '答题记录ID',
  `question_item_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '小题ID',
  `user_answer` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户答案',
  `is_correct` tinyint(1) NOT NULL COMMENT '是否正确',
  `time_spent` int(11) NOT NULL COMMENT '答题耗时(秒)',
  `answered_at` timestamp(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '答题时间',
  PRIMARY KEY (`answer_id`) USING BTREE,
  INDEX `idx_record`(`record_id`) USING BTREE,
  INDEX `idx_question_item`(`question_item_id`) USING BTREE,
  INDEX `idx_answered_time`(`answered_at`) USING BTREE,
  CONSTRAINT `user_answer_details_ibfk_1` FOREIGN KEY (`record_id`) REFERENCES `user_question_records` (`record_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `user_answer_details_ibfk_2` FOREIGN KEY (`question_item_id`) REFERENCES `question_items` (`item_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '答题详情表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for user_question_records
-- ----------------------------
DROP TABLE IF EXISTS `user_question_records`;
CREATE TABLE `user_question_records`  (
  `record_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '记录UUID',
  `user_id` int(11) NOT NULL COMMENT '用户ID',
  `question_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '题目ID',
  `status` enum('not-done','done') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'not-done' COMMENT '完成状态',
  `last_result` enum('correct','wrong') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '最近结果',
  `last_attempt_date` datetime(0) NULL DEFAULT NULL COMMENT '最近答题时间',
  `correct_count` int(11) NULL DEFAULT 0 COMMENT '正确次数',
  `wrong_count` int(11) NULL DEFAULT 0 COMMENT '错误次数',
  `is_favorited` tinyint(1) NULL DEFAULT 0 COMMENT '是否收藏',
  `created_at` timestamp(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '首次答题时间',
  `updated_at` timestamp(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`record_id`) USING BTREE,
  UNIQUE INDEX `uk_user_question`(`user_id`, `question_id`) USING BTREE,
  INDEX `question_id`(`question_id`) USING BTREE,
  INDEX `idx_user_status`(`user_id`, `status`) USING BTREE,
  INDEX `idx_favorited`(`is_favorited`) USING BTREE,
  INDEX `idx_attempt_date`(`last_attempt_date`) USING BTREE,
  CONSTRAINT `user_question_records_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `userss` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `user_question_records_ibfk_2` FOREIGN KEY (`question_id`) REFERENCES `questions` (`question_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户答题记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for user_vocabulary
-- ----------------------------
DROP TABLE IF EXISTS `user_vocabulary`;
CREATE TABLE `user_vocabulary`  (
  `vocab_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '生词记录UUID',
  `user_id` int(11) NOT NULL COMMENT '用户ID',
  `word_id` int(11) NOT NULL COMMENT '单词id',
  `translation` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '翻译',
  `source_question_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '来源题目ID',
  `if_mastered` tinyint(1) NULL DEFAULT 0 COMMENT '是否已掌握',
  PRIMARY KEY (`vocab_id`) USING BTREE,
  UNIQUE INDEX `uk_user_word`(`user_id`, `word_id`) USING BTREE,
  INDEX `source_question_id`(`source_question_id`) USING BTREE,
  INDEX `word_id`(`word_id`) USING BTREE,
  INDEX `idx_user_mastered`(`user_id`, `if_mastered`) USING BTREE,
  CONSTRAINT `user_vocabulary_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `userss` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `user_vocabulary_ibfk_2` FOREIGN KEY (`source_question_id`) REFERENCES `questions` (`question_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `user_vocabulary_ibfk_3` FOREIGN KEY (`word_id`) REFERENCES `words` (`word_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户生词表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for user_word_progress
-- ----------------------------
DROP TABLE IF EXISTS `user_word_progress`;
CREATE TABLE `user_word_progress`  (
  `progress_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '进度ID',
  `user_id` int(11) NOT NULL COMMENT '用户ID',
  `word_id` int(11) NOT NULL COMMENT '单词ID',
  `stage` enum('0','1','2','3') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '0' COMMENT '学习阶段(0=未学,1=新学,2=复习,3=掌握)',
  `last_review_time` datetime(0) NULL DEFAULT NULL COMMENT '上次复习时间',
  `next_review_time` datetime(0) NULL DEFAULT NULL COMMENT '下次复习时间',
  `review_count` int(11) NULL DEFAULT 0 COMMENT '复习次数',
  PRIMARY KEY (`progress_id`) USING BTREE,
  UNIQUE INDEX `uk_user_word`(`user_id`, `word_id`) USING BTREE,
  INDEX `word_id`(`word_id`) USING BTREE,
  INDEX `idx_next_review`(`next_review_time`) USING BTREE,
  INDEX `idx_stage`(`stage`) USING BTREE,
  CONSTRAINT `user_word_progress_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `userss` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `user_word_progress_ibfk_2` FOREIGN KEY (`word_id`) REFERENCES `words` (`word_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户单词进度表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名（唯一）',
  `user_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户状态',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '加密密码',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '手机号',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '头像',
  `join_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '注册时间',
  `signature` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '个性签名',
  `location` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '地区',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_username`(`username`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of users
-- ----------------------------
INSERT INTO `users` VALUES (1, 'jorlin', NULL, '666666', NULL, NULL, NULL, '2025-12-10 21:15:54', NULL, NULL);
INSERT INTO `users` VALUES (2, 'jaychou', NULL, '$2a$10$QwFWcAH4Ak9bnxXHce.EoORtvQZIY.bzi1KW67UijTcLMs8ds/Bz.', NULL, NULL, NULL, '2025-12-11 00:02:59', NULL, NULL);
INSERT INTO `users` VALUES (3, 'jooo', NULL, '$2a$10$AIo6Y/qpSPK4bg2YKPQRTeCh39T0NzXUsANxWXI/mZa979Uqe350S', NULL, NULL, NULL, '2025-12-11 00:10:29', NULL, NULL);
INSERT INTO `users` VALUES (4, 'jayjay', NULL, '$2a$10$VI0rRvgR7nTT9DRihTky/.v2rgUJK6vw278M7jW5M/OKD2Bx/vNzS', NULL, NULL, NULL, '2025-12-11 00:27:57', NULL, NULL);
INSERT INTO `users` VALUES (5, 'wumeiying', NULL, '$2a$10$W9G5FuGLEnjtx/tNopAqY.J9KAeTYdEKsu0JyR9Z3RXgSqzPvu2r6', NULL, NULL, NULL, '2025-12-11 08:38:56', NULL, NULL);

-- ----------------------------
-- Table structure for userss
-- ----------------------------
DROP TABLE IF EXISTS `userss`;
CREATE TABLE `userss`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '用户唯一ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '头像URL',
  `user_status` enum('沉迷学习','忙','摸鱼','闭关','美滋滋','裂开','疲惫','emo','bot','元气满满') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `signature` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `streak` int(11) NULL DEFAULT 0 COMMENT '连续打卡天数',
  `email` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `joinTime` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `phone` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `user_name`(`username`) USING BTREE,
  INDEX `idx_username`(`username`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of userss
-- ----------------------------
INSERT INTO `userss` VALUES (2, 'zhangsan', NULL, NULL, NULL, 0, NULL, NULL, NULL, '666666', NULL);

-- ----------------------------
-- Table structure for word_types
-- ----------------------------
DROP TABLE IF EXISTS `word_types`;
CREATE TABLE `word_types`  (
  `type_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '词汇类型ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '类型名称',
  PRIMARY KEY (`type_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '词汇类型表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for words
-- ----------------------------
DROP TABLE IF EXISTS `words`;
CREATE TABLE `words`  (
  `word_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '单词ID',
  `part_of_speech` enum('n.','adj.','v.','adv.','pron.','num.','art.','prep.','conj.','int','quantifiers','onomatopoeic') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '词性',
  `word` varchar(80) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '单词拼写',
  `phonetic` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '音标',
  `definition` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '中文释义',
  `example` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '英文例句',
  `type_id` int(11) NOT NULL COMMENT '词汇类型ID',
  `synonyms` json NULL COMMENT '同义词数组',
  `antonyms` json NULL COMMENT '反义词数组',
  `usage_notes` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '用法说明',
  PRIMARY KEY (`word_id`) USING BTREE,
  UNIQUE INDEX `uk_word_type`(`word`, `part_of_speech`) USING BTREE,
  INDEX `type_id`(`type_id`) USING BTREE,
  CONSTRAINT `words_ibfk_1` FOREIGN KEY (`type_id`) REFERENCES `word_types` (`type_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '单词表' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
