const express = require('express');
const router = express.Router();
const { v4: uuidv4 } = require('uuid');
const { dbRun, dbGet, dbAll } = require('../config/database');
const { authMiddleware } = require('../middleware/auth');

router.use(authMiddleware);

// GET /api/ai-chat/sessions — 获取用户所有会话
router.get('/sessions', async (req, res) => {
  try {
    const sessions = await dbAll(
      'SELECT * FROM ai_chat_sessions WHERE user_id = ? ORDER BY updated_at DESC',
      [req.userId]
    );
    res.json({ code: 200, message: 'success', data: sessions });
  } catch (error) {
    console.error('Get sessions error:', error);
    res.status(500).json({ code: 500, message: '获取会话列表失败' });
  }
});

// POST /api/ai-chat/sessions — 创建新会话
router.post('/sessions', async (req, res) => {
  try {
    const sessionId = uuidv4();
    const { title } = req.body || {};
    await dbRun(
      'INSERT INTO ai_chat_sessions (session_id, user_id, title) VALUES (?, ?, ?)',
      [sessionId, req.userId, title || null]
    );
    const session = await dbGet(
      'SELECT * FROM ai_chat_sessions WHERE session_id = ?',
      [sessionId]
    );
    res.json({ code: 200, message: 'success', data: session });
  } catch (error) {
    console.error('Create session error:', error);
    res.status(500).json({ code: 500, message: '创建会话失败' });
  }
});

// GET /api/ai-chat/sessions/:sessionId/messages — 获取会话消息
router.get('/sessions/:sessionId/messages', async (req, res) => {
  try {
    const { sessionId } = req.params;
    const session = await dbGet(
      'SELECT * FROM ai_chat_sessions WHERE session_id = ? AND user_id = ?',
      [sessionId, req.userId]
    );
    if (!session) {
      return res.status(404).json({ code: 404, message: '会话不存在' });
    }
    const messages = await dbAll(
      'SELECT * FROM ai_chat_messages WHERE session_id = ? ORDER BY created_at ASC',
      [sessionId]
    );
    res.json({ code: 200, message: 'success', data: messages });
  } catch (error) {
    console.error('Get messages error:', error);
    res.status(500).json({ code: 500, message: '获取消息失败' });
  }
});

// POST /api/ai-chat/sessions/:sessionId/messages — 保存消息
router.post('/sessions/:sessionId/messages', async (req, res) => {
  try {
    const { sessionId } = req.params;
    const { role, content } = req.body || {};
    if (!role || !content) {
      return res.status(400).json({ code: 400, message: 'role和content不能为空' });
    }
    const session = await dbGet(
      'SELECT * FROM ai_chat_sessions WHERE session_id = ? AND user_id = ?',
      [sessionId, req.userId]
    );
    if (!session) {
      return res.status(404).json({ code: 404, message: '会话不存在' });
    }
    await dbRun(
      'INSERT INTO ai_chat_messages (session_id, role, content) VALUES (?, ?, ?)',
      [sessionId, role, content]
    );
    await dbRun(
      'UPDATE ai_chat_sessions SET message_count = message_count + 1, updated_at = CURRENT_TIMESTAMP WHERE session_id = ?',
      [sessionId]
    );
    res.json({ code: 200, message: 'success' });
  } catch (error) {
    console.error('Save message error:', error);
    res.status(500).json({ code: 500, message: '保存消息失败' });
  }
});

// PUT /api/ai-chat/sessions/:sessionId — 更新会话标题
router.put('/sessions/:sessionId', async (req, res) => {
  try {
    const { sessionId } = req.params;
    const { title } = req.body || {};
    const session = await dbGet(
      'SELECT * FROM ai_chat_sessions WHERE session_id = ? AND user_id = ?',
      [sessionId, req.userId]
    );
    if (!session) {
      return res.status(404).json({ code: 404, message: '会话不存在' });
    }
    await dbRun(
      'UPDATE ai_chat_sessions SET title = ?, updated_at = CURRENT_TIMESTAMP WHERE session_id = ?',
      [title, sessionId]
    );
    res.json({ code: 200, message: 'success' });
  } catch (error) {
    console.error('Update session error:', error);
    res.status(500).json({ code: 500, message: '更新会话失败' });
  }
});

// DELETE /api/ai-chat/sessions/:sessionId — 删除会话
router.delete('/sessions/:sessionId', async (req, res) => {
  try {
    const { sessionId } = req.params;
    const session = await dbGet(
      'SELECT * FROM ai_chat_sessions WHERE session_id = ? AND user_id = ?',
      [sessionId, req.userId]
    );
    if (!session) {
      return res.status(404).json({ code: 404, message: '会话不存在' });
    }
    await dbRun('DELETE FROM ai_chat_messages WHERE session_id = ?', [sessionId]);
    await dbRun('DELETE FROM ai_chat_sessions WHERE session_id = ?', [sessionId]);
    res.json({ code: 200, message: 'success' });
  } catch (error) {
    console.error('Delete session error:', error);
    res.status(500).json({ code: 500, message: '删除会话失败' });
  }
});

module.exports = router;
