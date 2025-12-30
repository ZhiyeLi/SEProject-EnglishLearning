-- 辅助视图与索引：用于单词打卡进度的去重统计与查询性能优化
-- 注意：不修改或删除原有表，仅新增视图与索引

SET FOREIGN_KEY_CHECKS=0;

-- 视图：用户已打卡的去重单词（按 word + part_of_speech 合并）
DROP VIEW IF EXISTS vw_user_passed_words;
CREATE VIEW vw_user_passed_words AS
SELECT
  up.user_id,
  up.type_id,
  MIN(w.word_id) AS word_id,
  w.word,
  w.part_of_speech,
  MAX(up.passed_date) AS passed_date,
  MAX(up.last_review_time) AS last_review_time,
  MAX(up.review_count) AS review_count
FROM user_word_progress up
JOIN words w ON up.word_id = w.word_id
WHERE up.stage >= 1
GROUP BY up.user_id, up.type_id, w.word, w.part_of_speech;

-- 视图：进度汇总（每个用户、每个类型的总已打卡数与最后打卡日期）
DROP VIEW IF EXISTS vw_user_progress_summary;
CREATE VIEW vw_user_progress_summary AS
SELECT
  user_id,
  type_id,
  COUNT(*) AS passed_count,
  MAX(passed_date) AS last_passed_date
FROM vw_user_passed_words
GROUP BY user_id, type_id;

-- 视图：未打卡的单词（按 word + part_of_speech 去重）
DROP VIEW IF EXISTS vw_user_unpassed_words;
CREATE VIEW vw_user_unpassed_words AS
SELECT
  w.type_id,
  w.word,
  w.part_of_speech,
  MIN(w.word_id) AS word_id
FROM words w
GROUP BY w.type_id, w.word, w.part_of_speech;

-- 为查询性能添加索引（不更改表结构）
CREATE INDEX IF NOT EXISTS idx_words_type_word_pos ON words(type_id, word, part_of_speech);
CREATE INDEX IF NOT EXISTS idx_progress_user_type_stage ON user_word_progress(user_id, type_id, stage);
CREATE INDEX IF NOT EXISTS idx_daily_record_user_type_date ON daily_study_record(user_id, type_id, study_date);

SET FOREIGN_KEY_CHECKS=1;
