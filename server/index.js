import express from 'express';
import path from 'path';
import { fileURLToPath } from 'url';
import sqlite3 from 'sqlite3';
import fs from 'fs';

const app = express();
app.use(express.json());

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const dataDir = path.join(__dirname, 'data');
const dbPath = path.join(dataDir, 'elw.sqlite');

if (!fs.existsSync(dbPath)) {
  console.error('❌ Database not found. Run `npm run init-db` in server/ first to create it.');
  process.exit(1);
}

const db = new sqlite3.Database(dbPath, (err) => {
  if (err) {
    console.error('Failed to open database:', err);
    process.exit(1);
  }
  db.run('PRAGMA foreign_keys = ON');
});

// Health check
app.get('/api/health', (req, res) => {
  db.get('SELECT 1', (err) => {
    if (err) return res.status(500).json({ status: 'error', message: err.message });
    res.json({ status: 'ok', timestamp: new Date().toISOString() });
  });
});

// Get all words (with pagination)
app.get('/api/words', (req, res) => {
  const limit = Math.min(parseInt(req.query.limit) || 100, 500);
  const offset = parseInt(req.query.offset) || 0;
  
  db.all(
    'SELECT word_id, word_content FROM words LIMIT ? OFFSET ?',
    [limit, offset],
    (err, rows) => {
      if (err) return res.status(500).json({ error: err.message });
      res.json({ data: rows || [], count: rows?.length || 0, limit, offset });
    }
  );
});

// Get word details (with pos and translations)
app.get('/api/words/:id', (req, res) => {
  const id = Number(req.params.id);
  if (isNaN(id)) return res.status(400).json({ error: 'Invalid word_id' });

  db.get('SELECT * FROM words WHERE word_id = ?', [id], (err, word) => {
    if (err) return res.status(500).json({ error: err.message });
    if (!word) return res.status(404).json({ error: 'Word not found' });

    db.all('SELECT pos_id, part_of_speech FROM word_pos WHERE word_id = ?', [id], (err, pos) => {
      if (err) pos = [];
      
      db.all(
        `SELECT wt.trans_id, wt.chinese_meaning, wt.example_sentence, wp.part_of_speech
         FROM word_translation wt
         JOIN word_pos wp ON wt.pos_id = wp.pos_id
         WHERE wt.word_id = ?
         ORDER BY wt.translation_order`,
        [id],
        (err, translations) => {
          if (err) translations = [];
          res.json({ word, pos_list: pos || [], translations: translations || [] });
        }
      );
    });
  });
});

// Get user info (with learning progress summary)
app.get('/api/users/:id', (req, res) => {
  const id = Number(req.params.id);
  if (isNaN(id)) return res.status(400).json({ error: 'Invalid user_id' });

  db.get(
    'SELECT user_id, username, nickname, email, avatar, create_time, update_time, status FROM user_base WHERE user_id = ?',
    [id],
    (err, row) => {
      if (err) return res.status(500).json({ error: err.message });
      if (!row) return res.status(404).json({ error: 'User not found' });

      // Get learning progress summary
      db.get(
        'SELECT COUNT(*) as total, SUM(progress_status) as completed FROM user_learning_progress WHERE user_id = ?',
        [id],
        (err, progress) => {
          if (err) progress = { total: 0, completed: 0 };
          res.json({ ...row, progress: progress || { total: 0, completed: 0 } });
        }
      );
    }
  );
});

// Create new user
app.post('/api/users', (req, res) => {
  const { username, password, nickname, email, phone, avatar } = req.body;
  
  if (!username || !password || !nickname || !email) {
    return res.status(400).json({ error: 'Missing required fields: username, password, nickname, email' });
  }

  const now = new Date().toISOString();
  db.run(
    `INSERT INTO user_base (username, password, nickname, email, phone, avatar, create_time, update_time, status)
     VALUES (?, ?, ?, ?, ?, ?, ?, ?, 1)`,
    [username, password, nickname, email, phone || null, avatar || null, now, now],
    function (err) {
      if (err) {
        if (err.message.includes('UNIQUE')) {
          return res.status(409).json({ error: 'Username or email already exists' });
        }
        return res.status(500).json({ error: err.message });
      }
      res.status(201).json({ user_id: this.lastID, username, nickname, email });
    }
  );
});

// Get user's word memory status
app.get('/api/users/:id/words/memory', (req, res) => {
  const userId = Number(req.params.id);
  if (isNaN(userId)) return res.status(400).json({ error: 'Invalid user_id' });

  db.all(
    `SELECT wms.id, wms.word_id, wms.is_remembered, w.word_content
     FROM word_memory_status wms
     JOIN words w ON wms.word_id = w.word_id
     WHERE wms.user_id = ?
     LIMIT 100`,
    [userId],
    (err, rows) => {
      if (err) return res.status(500).json({ error: err.message });
      res.json({ data: rows || [], count: rows?.length || 0 });
    }
  );
});

// Update user's word memory status
app.post('/api/users/:id/words/:wordId/remember', (req, res) => {
  const userId = Number(req.params.id);
  const wordId = Number(req.params.wordId);
  const { is_remembered } = req.body;

  if (isNaN(userId) || isNaN(wordId)) {
    return res.status(400).json({ error: 'Invalid user_id or word_id' });
  }

  db.run(
    `INSERT INTO word_memory_status (word_id, user_id, is_remembered)
     VALUES (?, ?, ?)
     ON CONFLICT(word_id, user_id) DO UPDATE SET is_remembered = ?`,
    [wordId, userId, is_remembered ? 1 : 0, is_remembered ? 1 : 0],
    function (err) {
      if (err) return res.status(500).json({ error: err.message });
      res.json({ success: true, message: 'Memory status updated' });
    }
  );
});

// Get user groups
app.get('/api/users/:id/groups', (req, res) => {
  const userId = Number(req.params.id);
  if (isNaN(userId)) return res.status(400).json({ error: 'Invalid user_id' });

  db.all(
    `SELECT ug.group_id, ug.group_name, ug.creator_id, ug.create_time, ugr.role
     FROM user_group_relation ugr
     JOIN user_group ug ON ugr.group_id = ug.group_id
     WHERE ugr.user_id = ? AND ug.status = 1`,
    [userId],
    (err, rows) => {
      if (err) return res.status(500).json({ error: err.message });
      res.json({ data: rows || [], count: rows?.length || 0 });
    }
  );
});

// Error handler
app.use((err, req, res, next) => {
  console.error(err);
  res.status(500).json({ error: 'Internal server error', message: err.message });
});

// 404 handler
app.use((req, res) => {
  res.status(404).json({ error: 'Endpoint not found', path: req.path });
});

const port = process.env.PORT || 3000;
app.listen(port, () => {
  console.log(`\n✓ Server listening on http://localhost:${port}`);
  console.log(`\nAvailable endpoints:`);
  console.log(`  GET  /api/health`);
  console.log(`  GET  /api/words?limit=100&offset=0`);
  console.log(`  GET  /api/words/:id`);
  console.log(`  GET  /api/users/:id`);
  console.log(`  POST /api/users`);
  console.log(`  GET  /api/users/:id/words/memory`);
  console.log(`  POST /api/users/:id/words/:wordId/remember`);
  console.log(`  GET  /api/users/:id/groups`);
});
