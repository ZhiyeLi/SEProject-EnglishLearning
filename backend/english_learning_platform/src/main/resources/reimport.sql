-- 重新导入数据库
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

-- 表 word_types 的数据
INSERT INTO word_types (type_id, name, description, total_words) VALUES (1, '基础单词', '适合初学者', 1000);
INSERT INTO word_types (type_id, name, description, total_words) VALUES (2, '四级单词', '大学英语四级', 1500);
INSERT INTO word_types (type_id, name, description, total_words) VALUES (3, '六级单词（四级进阶）', '大学英语六级', 2000);
INSERT INTO word_types (type_id, name, description, total_words) VALUES (4, '托福雅思', '出国考试必备', 2500);

-- 表 words 的数据
INSERT INTO words (word_id, word, part_of_speech, phonetic, definition, example, type_id, synonyms, antonyms, usage_notes, audio_url, image_url) VALUES (1, 'hello', 'interjection', '/həˈloʊ/', '用于问候或引起注意', 'Hello! How are you today?', 1, 'hi, hey, greetings', 'goodbye, bye', '最常见的英语问候语', NULL, NULL);
INSERT INTO words (word_id, word, part_of_speech, phonetic, definition, example, type_id, synonyms, antonyms, usage_notes, audio_url, image_url) VALUES (2, 'book', 'noun', '/bʊk/', '一本书;印刷或书写的文学作品', 'I am reading an interesting book.', 1, 'volume, publication, work', '', '可数名词', NULL, NULL);
INSERT INTO words (word_id, word, part_of_speech, phonetic, definition, example, type_id, synonyms, antonyms, usage_notes, audio_url, image_url) VALUES (3, 'study', 'verb', '/ˈstʌdi/', '学习;研究', 'I study English every day.', 1, 'learn, research, examine', 'ignore, neglect', '常见动词,后可接宾语', NULL, NULL);
INSERT INTO words (word_id, word, part_of_speech, phonetic, definition, example, type_id, synonyms, antonyms, usage_notes, audio_url, image_url) VALUES (4, 'accomplish', 'verb', '/əˈkɑːmplɪʃ/', '完成;达到;实现', 'We have accomplished our mission successfully.', 2, 'achieve, complete, fulfill', 'fail, abandon', '正式用语,表示成功完成某事', NULL, NULL);
INSERT INTO words (word_id, word, part_of_speech, phonetic, definition, example, type_id, synonyms, antonyms, usage_notes, audio_url, image_url) VALUES (5, 'opportunity', 'noun', '/ˌɑːpərˈtuːnəti/', '机会;时机', 'This is a great opportunity to learn new skills.', 2, 'chance, occasion, opening', 'misfortune, disadvantage', '可数名词,常与介词for连用', NULL, NULL);
INSERT INTO words (word_id, word, part_of_speech, phonetic, definition, example, type_id, synonyms, antonyms, usage_notes, audio_url, image_url) VALUES (6, 'methodology', 'noun', '/ˌmeθəˈdɑːlədʒi/', '方法论;方法学', 'The research methodology is very rigorous.', 3, 'approach, system, procedure', '', '学术用语,常用于研究领域', NULL, NULL);
INSERT INTO words (word_id, word, part_of_speech, phonetic, definition, example, type_id, synonyms, antonyms, usage_notes, audio_url, image_url) VALUES (7, 'phenomenon', 'noun', '/fəˈnɑːmɪnən/', '现象;杰出的人或事物', 'This is an interesting social phenomenon.', 3, 'occurrence, event, fact', '', '复数形式为phenomena', NULL, NULL);
INSERT INTO words (word_id, word, part_of_speech, phonetic, definition, example, type_id, synonyms, antonyms, usage_notes, audio_url, image_url) VALUES (8, 'sophisticated', 'adjective', '/səˈfɪstɪkeɪtɪd/', '复杂的;精致的;老练的', 'The device uses sophisticated technology.', 4, 'complex, advanced, refined', 'simple, primitive, naive', '可用于描述人或事物', NULL, NULL);
INSERT INTO words (word_id, word, part_of_speech, phonetic, definition, example, type_id, synonyms, antonyms, usage_notes, audio_url, image_url) VALUES (9, 'integral', 'adjective', '/ˈɪntɪɡrəl/', '完整的;必需的;构成整体所必需的', 'Exercise is an integral part of a healthy lifestyle.', 4, 'essential, fundamental, vital', 'unnecessary, optional', '常与介词to连用', NULL, NULL);

SET FOREIGN_KEY_CHECKS=1;
