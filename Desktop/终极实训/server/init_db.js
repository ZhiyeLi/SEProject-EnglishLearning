import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';
import sqlite3 from 'sqlite3';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const dataDir = path.join(__dirname, 'data');
const dbPath = path.join(dataDir, 'elw.sqlite');

fs.mkdirSync(dataDir, { recursive: true });

const db = new sqlite3.Database(dbPath, (err) => {
  if (err) {
    console.error('Failed to open database:', err);
    process.exit(1);
  }
  console.log('✓ Opened SQLite DB at', dbPath);
});

// Enable foreign keys
db.run('PRAGMA foreign_keys = ON');

const createTableStatements = [
  // User Management
  `CREATE TABLE IF NOT EXISTS user_base (
    user_id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT NOT NULL UNIQUE,
    password TEXT NOT NULL,
    nickname TEXT NOT NULL,
    email TEXT NOT NULL UNIQUE,
    phone TEXT,
    avatar TEXT,
    create_time TEXT NOT NULL,
    update_time TEXT NOT NULL,
    status INTEGER NOT NULL DEFAULT 1
  );`,

  `CREATE TABLE IF NOT EXISTS user_learning_preference (
    preference_id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL UNIQUE,
    word_level TEXT,
    content_type TEXT,
    learning_time TEXT,
    update_time TEXT NOT NULL,
    FOREIGN KEY(user_id) REFERENCES user_base(user_id) ON DELETE CASCADE
  );`,

  `CREATE TABLE IF NOT EXISTS user_learning_progress (
    progress_id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    resource_type TEXT NOT NULL,
    resource_id INTEGER NOT NULL,
    progress_status INTEGER NOT NULL DEFAULT 0,
    finish_time TEXT,
    FOREIGN KEY(user_id) REFERENCES user_base(user_id) ON DELETE CASCADE
  );`,

  `CREATE INDEX IF NOT EXISTS idx_user_resource 
    ON user_learning_progress(user_id, resource_type, resource_id);`,

  // Social Features
  `CREATE TABLE IF NOT EXISTS user_group (
    group_id INTEGER PRIMARY KEY AUTOINCREMENT,
    group_name TEXT NOT NULL,
    creator_id INTEGER NOT NULL,
    create_time TEXT NOT NULL,
    status INTEGER NOT NULL DEFAULT 1,
    FOREIGN KEY(creator_id) REFERENCES user_base(user_id) ON DELETE CASCADE
  );`,

  `CREATE TABLE IF NOT EXISTS user_group_relation (
    group_relation_id INTEGER PRIMARY KEY AUTOINCREMENT,
    group_id INTEGER NOT NULL,
    user_id INTEGER NOT NULL,
    role INTEGER NOT NULL DEFAULT 1,
    join_time TEXT NOT NULL,
    UNIQUE(group_id, user_id),
    FOREIGN KEY(group_id) REFERENCES user_group(group_id) ON DELETE CASCADE,
    FOREIGN KEY(user_id) REFERENCES user_base(user_id) ON DELETE CASCADE
  );`,

  `CREATE TABLE IF NOT EXISTS group_message (
    message_id INTEGER PRIMARY KEY AUTOINCREMENT,
    group_id INTEGER NOT NULL,
    sender_id INTEGER NOT NULL,
    content TEXT,
    message_type INTEGER NOT NULL DEFAULT 1,
    file_url TEXT,
    file_name TEXT,
    send_time TEXT NOT NULL,
    is_deleted INTEGER NOT NULL DEFAULT 0,
    FOREIGN KEY(group_id) REFERENCES user_group(group_id) ON DELETE CASCADE,
    FOREIGN KEY(sender_id) REFERENCES user_base(user_id) ON DELETE CASCADE
  );`,

  `CREATE INDEX IF NOT EXISTS idx_message_time 
    ON group_message(group_id, send_time DESC);`,

  // Word Library
  `CREATE TABLE IF NOT EXISTS words (
    word_id INTEGER PRIMARY KEY AUTOINCREMENT,
    word_content TEXT NOT NULL UNIQUE
  );`,

  `CREATE TABLE IF NOT EXISTS word_pos (
    pos_id INTEGER PRIMARY KEY AUTOINCREMENT,
    word_id INTEGER NOT NULL,
    part_of_speech TEXT NOT NULL,
    FOREIGN KEY(word_id) REFERENCES words(word_id) ON DELETE CASCADE
  );`,

  `CREATE INDEX IF NOT EXISTS idx_pos_word_id ON word_pos(word_id);`,

  `CREATE TABLE IF NOT EXISTS word_translation (
    trans_id INTEGER PRIMARY KEY AUTOINCREMENT,
    word_id INTEGER NOT NULL,
    pos_id INTEGER NOT NULL,
    chinese_meaning TEXT NOT NULL,
    translation_order INTEGER DEFAULT 1,
    example_sentence TEXT,
    FOREIGN KEY(word_id) REFERENCES words(word_id) ON DELETE CASCADE,
    FOREIGN KEY(pos_id) REFERENCES word_pos(pos_id) ON DELETE CASCADE
  );`,

  `CREATE INDEX IF NOT EXISTS idx_trans_word_id ON word_translation(word_id);`,
  `CREATE INDEX IF NOT EXISTS idx_trans_pos_id ON word_translation(pos_id);`,

  `CREATE TABLE IF NOT EXISTS word_pos_changes (
    change_id INTEGER PRIMARY KEY AUTOINCREMENT,
    word_id INTEGER NOT NULL,
    pos_id INTEGER NOT NULL,
    change_form TEXT NOT NULL,
    change_name TEXT NOT NULL,
    change_type TEXT,
    FOREIGN KEY(word_id) REFERENCES words(word_id) ON DELETE CASCADE,
    FOREIGN KEY(pos_id) REFERENCES word_pos(pos_id) ON DELETE CASCADE
  );`,

  `CREATE TABLE IF NOT EXISTS word_phrases (
    phrase_id INTEGER PRIMARY KEY AUTOINCREMENT,
    word_id INTEGER NOT NULL,
    phrase_content TEXT NOT NULL,
    phrase_function TEXT NOT NULL,
    phrase_meaning TEXT NOT NULL,
    FOREIGN KEY(word_id) REFERENCES words(word_id) ON DELETE CASCADE
  );`,

  `CREATE TABLE IF NOT EXISTS word_memory_status (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    word_id INTEGER NOT NULL,
    user_id INTEGER NOT NULL,
    is_remembered INTEGER NOT NULL DEFAULT 0,
    UNIQUE(word_id, user_id),
    FOREIGN KEY(word_id) REFERENCES words(word_id) ON DELETE CASCADE,
    FOREIGN KEY(user_id) REFERENCES user_base(user_id) ON DELETE CASCADE
  );`,

  // Audit log (NEW)
  `CREATE TABLE IF NOT EXISTS user_audit_log (
    log_id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    action TEXT NOT NULL,
    old_value TEXT,
    new_value TEXT,
    changed_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY(user_id) REFERENCES user_base(user_id) ON DELETE CASCADE
  );`
];

function runStatements(stmts, i = 0) {
  if (i >= stmts.length) {
    console.log('✓ Database initialization complete.');
    db.close();
    return;
  }
  
  db.exec(stmts[i], (err) => {
    if (err && !err.message.includes('already exists')) {
      console.error('❌ Error running statement:', err.message);
      process.exit(1);
    }
    if (i % 5 === 0) process.stdout.write('.');
    runStatements(stmts, i + 1);
  });
}

runStatements(createTableStatements);
