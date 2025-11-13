import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';
import sqlite3 from 'sqlite3';
import csv from 'csv-parse/sync.js';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const dbPath = path.join(__dirname, 'data', 'elw.sqlite');

const db = new sqlite3.Database(dbPath, (err) => {
  if (err) {
    console.error('❌ 数据库打开失败:', err);
    process.exit(1);
  }
  console.log('✓ 数据库连接成功');
});

db.run('PRAGMA foreign_keys = ON');

/**
 * 从 CSV 文件导入单词
 * CSV 格式：word_content,word_level,part_of_speech,chinese_meaning,example_sentence,pos_changes,phrases
 * pos_changes 格式: "changed_form1|change_name1;changed_form2|change_name2"
 * phrases 格式: "phrase1|function1|meaning1;phrase2|function2|meaning2"
 */
function importWordsFromCSV(csvFilePath) {
  if (!fs.existsSync(csvFilePath)) {
    console.error(`❌ 文件不存在: ${csvFilePath}`);
    process.exit(1);
  }

  const fileContent = fs.readFileSync(csvFilePath, 'utf-8');
  const records = csv.parse(fileContent, {
    columns: true,
    skip_empty_lines: true,
  });

  console.log(`📖 读取了 ${records.length} 条记录`);

  let successCount = 0;
  let errorCount = 0;
  let pendingOperations = 0;

  // 使用事务提高性能
  db.run('BEGIN TRANSACTION', (err) => {
    if (err) {
      console.error('❌ 事务开始失败:', err);
      process.exit(1);
    }

    records.forEach((record, index) => {
      const {
        word_content,
        word_level,
        part_of_speech,
        chinese_meaning,
        example_sentence,
        pos_changes,
        phrases,
      } = record;

      // 验证必填字段
      if (!word_content) {
        console.warn(`⚠️  第 ${index + 2} 行: 单词内容为空，跳过`);
        errorCount++;
        return;
      }

      pendingOperations++;

      // 1. 插入单词
      db.run(
        'INSERT OR IGNORE INTO words (word_content) VALUES (?)',
        [word_content],
        function(err) {
          if (err) {
            console.error(`❌ 插入单词失败 (${word_content}):`, err.message);
            errorCount++;
            pendingOperations--;
            return;
          }

          const wordId = this.lastID || word_content;

          // 2. 插入难度等级
          if (word_level) {
            db.run(
              'INSERT OR IGNORE INTO note (word_id, word_level) VALUES (?, ?)',
              [wordId, word_level],
              (err) => {
                if (err && !err.message.includes('UNIQUE')) {
                  console.error(`❌ 插入难度等级失败:`, err.message);
                }
              }
            );
          }

          // 3. 插入词性、翻译、词形变化、短语
          if (part_of_speech) {
            const posList = part_of_speech.split(';').map(p => p.trim()).filter(p => p);
            
            posList.forEach(pos => {
              db.run(
                'INSERT OR IGNORE INTO word_pos (word_id, part_of_speech) VALUES (?, ?)',
                [wordId, pos],
                function(err) {
                  if (err) {
                    console.error(`❌ 插入词性失败:`, err.message);
                    return;
                  }

                  const posId = this.lastID;

                  // 4. 插入翻译
                  if (chinese_meaning) {
                    db.run(
                      'INSERT INTO word_translation (word_id, pos_id, chinese_meaning, example_sentence) VALUES (?, ?, ?, ?)',
                      [wordId, posId, chinese_meaning, example_sentence || ''],
                      (err) => {
                        if (err) {
                          console.error(`❌ 插入翻译失败:`, err.message);
                        }
                      }
                    );
                  }

                  // 5. 插入词形变化 (格式: "changed_form1|change_name1;changed_form2|change_name2")
                  if (pos_changes) {
                    const changesList = pos_changes.split(';').map(c => c.trim()).filter(c => c);
                    changesList.forEach(changeItem => {
                      const [changeForm, changeName] = changeItem.split('|').map(x => x.trim());
                      if (changeForm && changeName) {
                        db.run(
                          'INSERT INTO word_pos_changes (word_id, pos_id, change_form, change_name) VALUES (?, ?, ?, ?)',
                          [wordId, posId, changeForm, changeName],
                          (err) => {
                            if (err) {
                              console.error(`❌ 插入词形变化失败:`, err.message);
                            }
                          }
                        );
                      }
                    });
                  }
                }
              );
            });
          }

          // 6. 插入短语 (格式: "phrase1|function1|meaning1;phrase2|function2|meaning2")
          if (phrases) {
            const phrasesList = phrases.split(';').map(p => p.trim()).filter(p => p);
            phrasesList.forEach(phraseItem => {
              const parts = phraseItem.split('|').map(x => x.trim());
              const [phraseContent, phraseFunction, phraseMeaning] = parts;
              if (phraseContent && phraseFunction && phraseMeaning) {
                db.run(
                  'INSERT INTO word_phrases (word_id, phrase_content, phrase_function, phrase_meaning) VALUES (?, ?, ?, ?)',
                  [wordId, phraseContent, phraseFunction, phraseMeaning],
                  (err) => {
                    if (err) {
                      console.error(`❌ 插入短语失败:`, err.message);
                    }
                  }
                );
              }
            });
          }

          successCount++;
          pendingOperations--;
        }
      );
    });

    // 提交事务
    const checkAndCommit = () => {
      if (pendingOperations === 0) {
        db.run('COMMIT', (err) => {
          if (err) {
            console.error('❌ 提交事务失败:', err);
            db.run('ROLLBACK');
          } else {
            console.log(`\n✅ 导入完成！`);
            console.log(`   成功: ${successCount}`);
            console.log(`   失败: ${errorCount}`);
          }
          db.close();
        });
      } else {
        setTimeout(checkAndCommit, 100);
      }
    };
    
    setTimeout(checkAndCommit, 500);
  });
}

/**
 * 从 JSON 数组导入单词
 * JSON 格式: [{ word_content, word_level, part_of_speech, chinese_meaning }, ...]
 */
function importWordsFromJSON(jsonFilePath) {
  if (!fs.existsSync(jsonFilePath)) {
    console.error(`❌ 文件不存在: ${jsonFilePath}`);
    process.exit(1);
  }

  const fileContent = fs.readFileSync(jsonFilePath, 'utf-8');
  let records;

  try {
    records = JSON.parse(fileContent);
  } catch (err) {
    console.error('❌ JSON 解析失败:', err.message);
    process.exit(1);
  }

  if (!Array.isArray(records)) {
    console.error('❌ JSON 必须是数组格式');
    process.exit(1);
  }

  console.log(`📖 读取了 ${records.length} 条记录`);

  let successCount = 0;
  let errorCount = 0;

  db.run('BEGIN TRANSACTION', (err) => {
    if (err) {
      console.error('❌ 事务开始失败:', err);
      process.exit(1);
    }

    let pendingOperations = 0;

    records.forEach((record, index) => {
      const {
        word_content,
        word_level,
        part_of_speech,
        chinese_meaning,
        example_sentence,
        pos_changes,
        phrases,
      } = record;

      if (!word_content) {
        console.warn(`⚠️  第 ${index + 1} 条: 单词内容为空，跳过`);
        errorCount++;
        return;
      }

      pendingOperations++;

      db.run(
        'INSERT OR IGNORE INTO words (word_content) VALUES (?)',
        [word_content],
        function(err) {
          if (err) {
            console.error(`❌ 插入单词失败 (${word_content}):`, err.message);
            errorCount++;
            pendingOperations--;
            return;
          }

          const wordId = this.lastID || word_content;

          // 插入难度等级
          if (word_level) {
            db.run(
              'INSERT OR IGNORE INTO note (word_id, word_level) VALUES (?, ?)',
              [wordId, word_level]
            );
          }

          // 插入词性、翻译、词形变化、短语
          if (part_of_speech) {
            const posList = Array.isArray(part_of_speech)
              ? part_of_speech
              : [part_of_speech];

            posList.forEach(pos => {
              db.run(
                'INSERT OR IGNORE INTO word_pos (word_id, part_of_speech) VALUES (?, ?)',
                [wordId, pos],
                function(err) {
                  if (err) {
                    console.error(`❌ 插入词性失败:`, err.message);
                    return;
                  }

                  const posId = this.lastID;

                  // 插入翻译
                  if (chinese_meaning) {
                    db.run(
                      'INSERT INTO word_translation (word_id, pos_id, chinese_meaning, example_sentence) VALUES (?, ?, ?, ?)',
                      [wordId, posId, chinese_meaning, example_sentence || '']
                    );
                  }

                  // 插入词形变化
                  if (pos_changes && Array.isArray(pos_changes)) {
                    pos_changes.forEach(change => {
                      const { change_form, change_name } = change;
                      if (change_form && change_name) {
                        db.run(
                          'INSERT INTO word_pos_changes (word_id, pos_id, change_form, change_name) VALUES (?, ?, ?, ?)',
                          [wordId, posId, change_form, change_name]
                        );
                      }
                    });
                  }
                }
              );
            });
          }

          // 插入短语
          if (phrases && Array.isArray(phrases)) {
            phrases.forEach(phrase => {
              const { phrase_content, phrase_function, phrase_meaning } = phrase;
              if (phrase_content && phrase_function && phrase_meaning) {
                db.run(
                  'INSERT INTO word_phrases (word_id, phrase_content, phrase_function, phrase_meaning) VALUES (?, ?, ?, ?)',
                  [wordId, phrase_content, phrase_function, phrase_meaning]
                );
              }
            });
          }

          successCount++;
          pendingOperations--;
        }
      );
    });

    // 提交事务
    const checkAndCommit = () => {
      if (pendingOperations === 0) {
        db.run('COMMIT', (err) => {
          if (err) {
            console.error('❌ 提交事务失败:', err);
            db.run('ROLLBACK');
          } else {
            console.log(`\n✅ 导入完成！`);
            console.log(`   成功: ${successCount}`);
            console.log(`   失败: ${errorCount}`);
          }
          db.close();
        });
      } else {
        setTimeout(checkAndCommit, 100);
      }
    };
    
    setTimeout(checkAndCommit, 500);
  });
}

// 主程序
const args = process.argv.slice(2);
if (args.length === 0) {
  console.log('❌ 请提供数据文件路径');
  console.log('\n📖 使用方式:');
  console.log('  node import_words.js <file_path>');
  console.log('\n📝 支持格式:');
  console.log('');
  console.log('━━━ CSV 格式 ━━━');
  console.log('列: word_content,word_level,part_of_speech,chinese_meaning,example_sentence,pos_changes,phrases');
  console.log('');
  console.log('  pos_changes 格式: "changed_form1|change_name1;changed_form2|change_name2"');
  console.log('    示例: "running|现在分词;run|原型"');
  console.log('');
  console.log('  phrases 格式: "phrase1|function1|meaning1;phrase2|function2|meaning2"');
  console.log('    示例: "run away|动词短语|逃跑;running mate|名词短语|竞选搭档"');
  console.log('');
  console.log('━━━ JSON 格式 ━━━');
  console.log(`{
  "word_content": "run",
  "word_level": "A1",
  "part_of_speech": ["verb", "noun"],
  "chinese_meaning": "跑；运行",
  "example_sentence": "I run every morning.",
  "pos_changes": [
    { "change_form": "running", "change_name": "现在分词" },
    { "change_form": "ran", "change_name": "过去式" }
  ],
  "phrases": [
    { "phrase_content": "run away", "phrase_function": "动词短语", "phrase_meaning": "逃跑" },
    { "phrase_content": "run into", "phrase_function": "动词短语", "phrase_meaning": "偶然遇到" }
  ]
}`);
  console.log('');
  console.log('📌 示例:');
  console.log('  node import_words.js ./words.csv');
  console.log('  node import_words.js ./words.json');
  process.exit(1);
}

const filePath = args[0];
const ext = path.extname(filePath).toLowerCase();

if (ext === '.csv') {
  importWordsFromCSV(filePath);
} else if (ext === '.json') {
  importWordsFromJSON(filePath);
} else {
  console.error(`❌ 不支持的文件格式: ${ext}`);
  console.error('仅支持 CSV 和 JSON 格式');
  process.exit(1);
}
