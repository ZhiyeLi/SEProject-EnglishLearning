const express = require('express');
const router = express.Router();
// 为了安全，通常应引入 require('../middleware/auth.js')，但这里作为示例保持简单

const RAG_SERVICE_URL = 'http://localhost:8001';

router.post('/rag_chat', async (req, res) => {
    try {
        const { message } = req.body;
        
        if (!message) {
            return res.status(400).json({ code: 400, message: 'Message is required' });
        }

        // 转发请求给 Python RAG 服务
        const response = await fetch(`${RAG_SERVICE_URL}/api/rag_chat`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ query: message })
        });

        if (!response.ok) {
            const error = await response.text();
            throw new Error(`RAG Error: ${response.status} - ${error}`);
        }

        const data = await response.json();
        
        res.json({
            code: 200,
            message: 'success',
            data: {
                reply: data.answer
            }
        });
    } catch (error) {
        console.error('RAG Error:', error);
        res.status(500).json({ code: 500, message: 'AI服务内部错误，请稍后再试' });
    }
});

module.exports = router;
