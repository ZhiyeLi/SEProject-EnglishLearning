import request from '../utils/request';

/**
 * 发送消息给 RAG 智能助教（JSON 模式，兼容保留）
 */
export function sendRagMessage(message) {
  return request({
    url: '/api/rag/rag_chat',
    method: 'post',
    timeout: 120000,
    data: { message }
  });
}

/**
 * 流式发送消息给 RAG 智能助教（SSE 模式）
 *
 * 使用 XMLHttpRequest 而非 fetch，因为 XHR 的 onprogress 事件
 * 会在每次收到数据块时触发，天然适合 SSE 流式场景。
 */
export function sendRagMessageStream(message, { onToken, onDone, onError }) {
  const xhr = new XMLHttpRequest();
  let lastIndex = 0;
  let tokenCount = 0;

  xhr.open('POST', 'http://localhost:8001/api/rag_chat');
  xhr.setRequestHeader('Content-Type', 'application/json');
  xhr.setRequestHeader('Accept', 'text/event-stream');

  xhr.onprogress = () => {
    const newData = xhr.responseText.substring(lastIndex);
    lastIndex = xhr.responseText.length;

    const lines = newData.split('\n');
    for (const line of lines) {
      if (line.startsWith('data: ')) {
        try {
          const data = JSON.parse(line.slice(6));
          if (data.token !== undefined) {
            tokenCount++;
            onToken(data.token);
          } else if (data.done) {
            onDone(data.session_id || '');
            return;
          } else if (data.error) {
            onError(new Error(data.error));
            return;
          }
        } catch (e) {
          // skip parse errors for incomplete lines
        }
      }
    }
  };

  xhr.onload = () => {
    if (xhr.status !== 200) {
      onError(new Error(`RAG service error: ${xhr.status}`));
      return;
    }
    if (tokenCount === 0) {
      // No streaming tokens received, try parsing full response as JSON fallback
      try {
        const data = JSON.parse(xhr.responseText);
        if (data.answer) {
          onToken(data.answer);
        }
      } catch (e) { /* ignore */ }
    }
    onDone('');
  };

  xhr.onerror = () => {
    onError(new Error('Network error connecting to RAG service'));
  };

  xhr.ontimeout = () => {
    onError(new Error('RAG service timeout'));
  };

  xhr.timeout = 120000;
  xhr.send(JSON.stringify({ query: message }));

  return xhr;
}
