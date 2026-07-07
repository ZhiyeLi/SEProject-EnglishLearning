import request from '../utils/request';

/**
 * 发送消息给 RAG 智能助教（JSON 模式，兼容保留）
 * @param {string} message 用户问题
 * @returns {Promise} 响应 promise
 */
export function sendRagMessage(message) {
  return request({
    url: '/api/rag/rag_chat',
    method: 'post',
    timeout: 120000,
    data: {
      message
    }
  });
}

/**
 * 流式发送消息给 RAG 智能助教（SSE 模式）
 * 通过 Vue dev server 代理转发到 Python RAG 服务
 *
 * @param {string} message 用户问题
 * @param {string|null} sessionId 会话 ID（用于多轮对话）
 * @param {object} callbacks
 * @param {function} callbacks.onToken 收到 token 时调用 (token: string)
 * @param {function} callbacks.onDone 完成时调用 (sessionId: string)
 * @param {function} callbacks.onError 出错时调用 (error: Error)
 * @returns {AbortController} 可用于取消请求
 */
export function sendRagMessageStream(message, sessionId, { onToken, onDone, onError }) {
  const controller = new AbortController();
  const RAG_SSE_URL = '/rag-sse/api/rag_chat';

  fetch(RAG_SSE_URL, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Accept': 'text/event-stream',
    },
    body: JSON.stringify({
      query: message,
      session_id: sessionId || null,
    }),
    signal: controller.signal,
  })
    .then(async (response) => {
      if (!response.ok) {
        throw new Error(`RAG service error: ${response.status}`);
      }
      const reader = response.body.getReader();
      const decoder = new TextDecoder();
      let buffer = '';

      let done = false;
      while (!done) {
        const chunk = await reader.read();
        done = chunk.done;
        if (done) break;
        const value = chunk.value;

        buffer += decoder.decode(value, { stream: true });
        // Split on \n, strip trailing \r for SSE spec compliance
        const lines = buffer.split('\n').map(l => l.replace(/\r$/, ''));
        buffer = lines.pop() || '';

        for (const line of lines) {
          if (line.startsWith('data: ')) {
            try {
              const data = JSON.parse(line.slice(6));
              if (data.token !== undefined) {
                onToken(data.token);
              } else if (data.done) {
                onDone(data.session_id || '');
              } else if (data.error) {
                onError(new Error(data.error));
              }
            } catch (e) {
              // Skip unparseable lines
            }
          }
        }
      }
      // Flush any remaining bytes in the TextDecoder
      buffer += decoder.decode();
      const remaining = buffer.split('\n').map(l => l.replace(/\r$/, ''));
      for (const line of remaining) {
        if (line.startsWith('data: ')) {
          try {
            const data = JSON.parse(line.slice(6));
            if (data.token !== undefined) onToken(data.token);
            if (data.done) onDone(data.session_id || '');
          } catch (e) { /* ignore */ }
        }
      }
    })
    .catch((err) => {
      if (err.name !== 'AbortError') {
        onError(err);
      }
    });

  return controller;
}
