SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 1. 用户基础信息表（user_base）
-- ----------------------------
DROP TABLE IF EXISTS `user_base`;
CREATE TABLE `user_base`  (
  `user_id` int NOT NULL AUTO_INCREMENT COMMENT ,
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户登录账号',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '加密存储的密码',
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户昵称',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用于验证和通知的邮箱',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '绑定手机号',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户头像图片地址',
  `create_time` datetime NOT NULL COMMENT '账号创建时间',
  `update_time` datetime NOT NULL COMMENT '账号信息最后更新时间',
  `status` tinyint NOT NULL COMMENT '账号状态（0=禁用，1=正常）',
  PRIMARY KEY (`user_id`) USING BTREE,
  UNIQUE INDEX `idx_username`(`username` ASC) USING BTREE,
  UNIQUE INDEX `idx_email`(`email` ASC) USING BTREE,
  UNIQUE INDEX `idx_phone`(`phone` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- 2. 用户学习偏好表（user_learning_preference）
-- ----------------------------
DROP TABLE IF EXISTS `user_learning_preference`;
CREATE TABLE `user_learning_preference`  (
  `preference_id` int NOT NULL AUTO_INCREMENT COMMENT '偏好记录唯一标识',
  `user_id` int NOT NULL COMMENT '关联用户ID',
  `word_level` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '单词记忆等级',
  `content_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '偏好的内容类型',
  `learning_time` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '常用学习时段',
  `update_time` datetime NOT NULL COMMENT '偏好信息更新时间',
  PRIMARY KEY (`preference_id`) USING BTREE,
  UNIQUE INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  CONSTRAINT `fk_preference_user_id` FOREIGN KEY (`user_id`) REFERENCES `user_base` (`user_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- 3. 用户学习进度表（user_learning_progress）
-- ----------------------------
DROP TABLE IF EXISTS `user_learning_progress`;
CREATE TABLE `user_learning_progress`  (
  `progress_id` int NOT NULL AUTO_INCREMENT COMMENT '进度记录唯一标识',
  `user_id` int NOT NULL COMMENT '关联用户ID',
  `resource_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '资源类型',
  `resource_id` int NOT NULL COMMENT '对应资源的唯一标识',
  `progress_status` tinyint NOT NULL COMMENT '进度状态（0=未完成，1=已完成）',
  `finish_time` datetime NULL DEFAULT NULL COMMENT '完成时间',
  PRIMARY KEY (`progress_id`) USING BTREE,
  INDEX `idx_user_resource`(`user_id` ASC, `resource_type` ASC, `resource_id` ASC) USING BTREE,
  CONSTRAINT `fk_progress_user_id` FOREIGN KEY (`user_id`) REFERENCES `user_base` (`user_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- 4. 用户群组表（user_group）
-- ----------------------------
DROP TABLE IF EXISTS `user_group`;
CREATE TABLE `user_group`  (
  `group_id` int NOT NULL AUTO_INCREMENT COMMENT '群组唯一标识',
  `group_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '群组名称',
  `creator_id` int NOT NULL COMMENT '群组创建者ID',
  `create_time` datetime NOT NULL COMMENT '群组创建时间',
  `status` tinyint NOT NULL COMMENT '群组状态（0=解散，1=正常）',
  PRIMARY KEY (`group_id`) USING BTREE,
  CONSTRAINT `fk_group_creator_id` FOREIGN KEY (`creator_id`) REFERENCES `user_base` (`user_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- 5. 用户群组关联表（user_group_relation）
-- ----------------------------
DROP TABLE IF EXISTS `user_group_relation`;
CREATE TABLE `user_group_relation`  (
  `group_relation_id` int NOT NULL AUTO_INCREMENT COMMENT '关联记录唯一标识',
  `group_id` int NOT NULL COMMENT '关联群组ID',
  `user_id` int NOT NULL COMMENT '关联用户ID',
  `role` tinyint NOT NULL COMMENT '用户在群组中的角色（1=普通成员，2=管理员，3=创建者）',
  `join_time` datetime NOT NULL COMMENT '加入群组时间',
  PRIMARY KEY (`group_relation_id`) USING BTREE,
  UNIQUE INDEX `idx_group_user`(`group_id` ASC, `user_id` ASC) USING BTREE,
  INDEX `idx_relation_user_id`(`user_id` ASC) USING BTREE,
  CONSTRAINT `fk_relation_group_id` FOREIGN KEY (`group_id`) REFERENCES `user_group` (`group_id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_relation_user_id` FOREIGN KEY (`user_id`) REFERENCES `user_base` (`user_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- 6. 群组消息表（group_message）
-- ----------------------------
DROP TABLE IF EXISTS `group_message`;
CREATE TABLE `group_message`  (
  `message_id` int NOT NULL AUTO_INCREMENT COMMENT '消息唯一标识',
  `group_id` int NOT NULL COMMENT '关联群组ID',
  `sender_id` int NOT NULL COMMENT '消息发送者ID',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文本消息内容（非文本类型时为空）',
  `message_type` int NOT NULL COMMENT '消息类型（1=文本，2=图片，3=文件）',
  `file_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '图片/文件的URL（非文本类型消息使用）',
  `file_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文件名称（非文本类型消息使用）',
  `send_time` datetime NOT NULL COMMENT '消息发送时间',
  `is_deleted` tinyint NOT NULL DEFAULT 0 COMMENT '消息是否被删除（0=未删除，1=已删除）',
  PRIMARY KEY (`message_id`) USING BTREE,
  INDEX `idx_message_group_id`(`group_id` ASC) USING BTREE,
  INDEX `idx_message_sender_id`(`sender_id` ASC) USING BTREE,
  CONSTRAINT `fk_message_group_id` FOREIGN KEY (`group_id`) REFERENCES `user_group` (`group_id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_message_sender_id` FOREIGN KEY (`sender_id`) REFERENCES `user_base` (`user_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- 7. 单词基础表（words）（原有）
-- ----------------------------
DROP TABLE IF EXISTS `words`;
CREATE TABLE `words`  (
  `word_id` int NOT NULL AUTO_INCREMENT COMMENT ,
  `word_content` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT ,
  PRIMARY KEY (`word_id`) USING BTREE,
  UNIQUE INDEX `idx_word_content`(`word_content` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- 8. 单词难度分级表（note）（原有）
-- ----------------------------
DROP TABLE IF EXISTS `note`;
CREATE TABLE `note`  (
  `level_id` int NOT NULL AUTO_INCREMENT COMMENT ,
  `word_id` int NOT NULL COMMENT ,
  `word_level` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT ,
  PRIMARY KEY (`level_id`) USING BTREE,
  INDEX `idx_note_word_id`(`word_id` ASC) USING BTREE,
  CONSTRAINT `fk_note_word_id` FOREIGN KEY (`word_id`) REFERENCES `words` (`word_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- 9. 单词词性表（word_pos）（原有）
-- ----------------------------
DROP TABLE IF EXISTS `word_pos`;
CREATE TABLE `word_pos`  (
  `pos_id` int NOT NULL AUTO_INCREMENT COMMENT ,
  `word_id` int NOT NULL COMMENT ,
  `part_of_speech` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT ,
  PRIMARY KEY (`pos_id`) USING BTREE,
  UNIQUE INDEX `idx_part_of_speech`(`part_of_speech` ASC) USING BTREE,
  INDEX `idx_pos_word_id`(`word_id` ASC) USING BTREE,
  CONSTRAINT `fk_pos_word_id` FOREIGN KEY (`word_id`) REFERENCES `words` (`word_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- 10. 单词翻译表（word_translation）（原有）
-- ----------------------------
DROP TABLE IF EXISTS `word_translation`;
CREATE TABLE `word_translation`  (
  `trans_id` int NOT NULL AUTO_INCREMENT COMMENT ,
  `word_id` int NOT NULL COMMENT ,
  `pos_id` int NOT NULL COMMENT ,
  `chinese_meaning` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT ,
  PRIMARY KEY (`trans_id`) USING BTREE,
  INDEX `idx_trans_word_id`(`word_id` ASC) USING BTREE,
  INDEX `idx_trans_pos_id`(`pos_id` ASC) USING BTREE,
  CONSTRAINT `fk_trans_word_id` FOREIGN KEY (`word_id`) REFERENCES `words` (`word_id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_trans_pos_id` FOREIGN KEY (`pos_id`) REFERENCES `word_pos` (`pos_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- 11. 单词词性变化表（word_pos_changes）（原有）
-- ----------------------------
DROP TABLE IF EXISTS `word_pos_changes`;
CREATE TABLE `word_pos_changes`  (
  `change_id` int NOT NULL AUTO_INCREMENT COMMENT ,
  `word_id` int NOT NULL COMMENT ,
  `pos_id` int NOT NULL COMMENT ,
  `change_form` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT ,
  `change_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT ,
  PRIMARY KEY (`change_id`) USING BTREE,
  INDEX `idx_change_word_id`(`word_id` ASC) USING BTREE,
  INDEX `idx_change_pos_id`(`pos_id` ASC) USING BTREE,
  CONSTRAINT `fk_change_word_id` FOREIGN KEY (`word_id`) REFERENCES `words` (`word_id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_change_pos_id` FOREIGN KEY (`pos_id`) REFERENCES `word_pos` (`pos_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- 12. 单词短语表（word_phrases）（原有）
-- ----------------------------
DROP TABLE IF EXISTS `word_phrases`;
CREATE TABLE `word_phrases`  (
  `phrase_id` int NOT NULL AUTO_INCREMENT COMMENT ,
  `word_id` int NOT NULL COMMENT ,
  `phrase_content` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT ,
  `phrase_function` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT ,
  `phrase_meaning` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT ,
  PRIMARY KEY (`phrase_id`) USING BTREE,
  INDEX `idx_phrase_word_id`(`word_id` ASC) USING BTREE,
  CONSTRAINT `fk_phrase_word_id` FOREIGN KEY (`word_id`) REFERENCES `words` (`word_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- 13. 单词记忆状态表（word_memory_status）（原有）
-- ----------------------------
DROP TABLE IF EXISTS `word_memory_status`;
CREATE TABLE `word_memory_status`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT ,
  `word_id` int NOT NULL COMMENT ,
  `user_id` int NOT NULL COMMENT ,
  `is_remembered` tinyint NOT NULL COMMENT ,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_word_user`(`word_id` ASC, `user_id` ASC) USING BTREE,
  INDEX `idx_memory_user_id`(`user_id` ASC) USING BTREE,
  CONSTRAINT `fk_memory_word_id` FOREIGN KEY (`word_id`) REFERENCES `words` (`word_id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_memory_user_id` FOREIGN KEY (`user_id`) REFERENCES `user_base` (`user_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;