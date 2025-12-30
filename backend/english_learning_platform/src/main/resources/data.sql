-- MySQL 数据导入语句
-- 生成时间: 2025-12-11 19:42:54

SET FOREIGN_KEY_CHECKS=0;

-- ❌ 已删除 sqlite_sequence 插入语句，MySQL 不需要也不支持这个表

-- 表 word_types 的数据
INSERT INTO word_types (type_id, name, description, total_words) VALUES (1, '基础词汇', '初级词汇基础', 8245);
INSERT INTO word_types (type_id, name, description, total_words) VALUES (2, '四级词汇', '大学英语四级', 13134);
INSERT INTO word_types (type_id, name, description, total_words) VALUES (3, '六级词汇', '大学英语六级', 1619);
INSERT INTO word_types (type_id, name, description, total_words) VALUES (4, '托福雅思词汇', '出国考试必备', 576);

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
INSERT INTO words (word_id, word, part_of_speech, phonetic, definition, example, type_id, synonyms, antonyms, usage_notes, audio_url, image_url) VALUES (10, 'algorithm', 'noun', '/ˈælɡərɪðəm/', '算法', 'This algorithm can solve the problem efficiently.', 4, 'procedure, process, formula', '', '计算机科学术语', NULL, NULL);
INSERT INTO words (word_id, word, part_of_speech, phonetic, definition, example, type_id, synonyms, antonyms, usage_notes, audio_url, image_url) VALUES (11, 'protocol', 'noun', '/ˈproʊtəkɔːl/', '协议;规程', 'We must follow the communication protocol.', 4, 'procedure, code, convention', '', '多用于技术和外交领域', NULL, NULL);

SET FOREIGN_KEY_CHECKS=1;