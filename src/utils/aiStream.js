/**
 * OpenAI 风格 SSE 流式输出解析（兼容 Deepbricks）
 *
 * 约定：
 * - 使用 /v1/chat/completions
 * - stream: true
 * - 服务端按 SSE 形式返回：data: {json}\n\n ... data: [DONE]
 */

// 清理回复中的噪音前缀
export function sanitizeChunk(chunk, existingText) {
  try {
    if (!chunk || typeof chunk !== "string") return chunk;
    if (!existingText || existingText.length === 0) {
      return chunk.replace(/^\s*assistant\s*[:\-\s]*\s*/i, "");
    }
    return chunk;
  } catch (e) {
    return chunk;
  }
}

// 发送到用户提供的 base URL（OpenAI 兼容接口）
export async function callApiStream({
  apiKey,
  baseUrl,
  model = "gpt-3.5-turbo",
  messages,
  onChunk,
  signal,
}) {
  if (!apiKey || !baseUrl) {
    throw new Error("请在代码中设置 API_KEY 和 BASE_URL");
  }
  if (!Array.isArray(messages) || messages.length === 0) {
    throw new Error("messages 不能为空");
  }
  if (typeof onChunk !== "function") {
    throw new Error("onChunk 必须是函数");
  }

  const url = baseUrl.replace(/\/$/, "") + "/v1/chat/completions";
  const payload = {
    model,
    messages,
    stream: true,
  };

  const res = await fetch(url, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${apiKey}`,
    },
    body: JSON.stringify(payload),
    signal,
  });

  if (!res.ok) {
    const text = await res.text();
    throw new Error(`API 错误: ${res.status} ${text}`);
  }

  if (!res.body) {
    throw new Error("API 响应无可读数据流");
  }

  const reader = res.body.getReader();
  const decoder = new TextDecoder("utf-8");
  let buffer = "";

  let isDone = false;
  while (!isDone) {
    const { value, done } = await reader.read();
    isDone = done;

    if (!value) continue;

    buffer += decoder.decode(value, { stream: true });

    // 尝试解析 data: 事件（OpenAI 风格）
    const parts = buffer.split("\n\n");
    buffer = parts.pop() || ""; // 留下不完整的部分

    for (const part of parts) {
      const line = part.trim();
      if (!line) continue;

      if (line.startsWith("data:")) {
        const data = line.replace(/^data:\s?/, "");

        if (data === "[DONE]") {
          return;
        }

        try {
          const parsed = JSON.parse(data);
          const choice = parsed?.choices?.[0];
          const delta = choice?.delta?.content || choice?.delta?.role;

          if (delta) {
            onChunk(delta);
            continue;
          }

          // 兼容少数实现：直接用 text 字段
          if (choice?.text) {
            onChunk(choice.text);
            continue;
          }
        } catch (e) {
          // 非 JSON：可能是普通文本
          onChunk(data);
        }
      } else {
        // 直接文本块
        onChunk(line);
      }
    }
  }
}
