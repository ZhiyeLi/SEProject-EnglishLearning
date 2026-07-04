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
 * 直连 Python RAG 服务，绕过 Spring Boot 代理
 *
 * @param {string} message 用户问题
 * @param {object} callbacks
 * @param {function} callbacks.onToken 收到 token 时调用 (token: string)
 * @param {function} callbacks.onDone 完成时调用 (sessionId: string)
 * @param {function} callbacks.onError 出错时调用 (error: Error)
 * @returns {AbortController} 可用于取消请求
 */
export function sendRagMessageStream(message, { onToken, onDone, onError }) {
  const controller = new AbortController();
  const RAG_SSE_URL = '/rag-sse/api/rag_chat';

  fetch(RAG_SSE_URL, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Accept': 'text/event-stream',
    },
    body: JSON.stringify({ query: message }),
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
        const lines = buffer.split('\n');
        // 保留最后一个可能不完整的行
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
              // 跳过解析失败的行（如空行或注释）
            }
          }
        }
      }
      // 处理 buffer 中剩余的内容
      if (buffer.startsWith('data: ')) {
        try {
          const data = JSON.parse(buffer.slice(6));
          if (data.token !== undefined) onToken(data.token);
          if (data.done) onDone(data.session_id || '');
        } catch (e) { /* ignore */ }
      }
    })
    .catch((err) => {
      if (err.name !== 'AbortError') {
        onError(err);
      }
    });

  return controller;
}
