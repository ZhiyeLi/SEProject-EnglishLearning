package com.example.english_learning_platform.controller;

import com.example.english_learning_platform.dto.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rag")
public class RagController {

    private static final Logger logger = LoggerFactory.getLogger(RagController.class);
    private static final String RAG_SERVICE_URL = "http://localhost:8001/api/rag_chat";

    private final RestTemplate restTemplate;

    public RagController() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);   // 连接超时 5s
        factory.setReadTimeout(60000);     // 读取超时 60s (Gemini API 可能较慢)
        this.restTemplate = new RestTemplate(factory);
    }

    @PostMapping("/rag_chat")
    public ApiResponse<Map<String, Object>> ragChat(@RequestBody Map<String, String> body) {
        try {
            String message = body.get("message");
            if (message == null || message.isBlank()) {
                return ApiResponse.error(400, "Message is required");
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("query", message);
            HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                RAG_SERVICE_URL, request, Map.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> data = new HashMap<>();
                data.put("reply", response.getBody().get("answer"));
                return ApiResponse.success(data);
            } else {
                logger.warn("RAG service returned non-2xx: {}", response.getStatusCode());
                return ApiResponse.error("AI服务响应异常");
            }
        } catch (Exception e) {
            logger.error("RAG request failed", e);
            return ApiResponse.error("AI服务内部错误，请稍后再试");
        }
    }

    @PostMapping("/analyze_question")
    public ApiResponse<Map<String, Object>> analyzeQuestion(@RequestBody Map<String, Object> body) {
        try {
            String prompt = buildAnalyzePrompt(body);
            if (prompt == null || prompt.isBlank()) {
                return ApiResponse.error(400, "Analyze context is required");
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("query", prompt);
            HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                RAG_SERVICE_URL, request, Map.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> data = new HashMap<>();
                Object answer = response.getBody().get("answer");
                data.put("answer", answer);
                data.put("reply", answer);
                return ApiResponse.success(data);
            }

            logger.warn("RAG analyze service returned non-2xx: {}", response.getStatusCode());
            return ApiResponse.error("AI服务响应异常");
        } catch (Exception e) {
            logger.error("RAG analyze request failed", e);
            return ApiResponse.error("AI服务内部错误，请稍后再试");
        }
    }

    private String buildAnalyzePrompt(Map<String, Object> body) {
        String mode = toText(body.get("mode"));
        Map<String, Object> question = asMap(body.get("question"));
        Map<String, Object> subItem = asMap(body.get("subItem"));
        String userAnswer = stringify(body.get("userAnswer"));
        String questionType = toText(subItem.get("itemType"));

        String sectionName = toText(question.get("sectionName"));
        String title = toText(question.get("title"));
        String header = joinNonEmpty(" - ", sectionName, title);
        String content = toText(subItem.get("content"));
        String materialText = toText(question.get("materialText"));
        String materialImage = toText(question.get("materialImage"));
        String audioUrl = toText(question.get("audioUrl"));
        String correctAnswer = stringify(subItem.get("correctAnswer"));
        String explanation = toText(subItem.get("explanation"));
        String optionsText = formatOptions(subItem.get("options"));

        if ("writing".equalsIgnoreCase(mode) || isWritingType(questionType)) {
            return "你是一位毒舌但极其负责、经验丰富的英语阅卷老手。现在要对比学生作答和官方范文，给出犀利但真能提分的点评。\n\n"
                + "所属部分：" + defaultText(header, "(未知)") + "\n\n"
                + "题目要求：\n" + defaultText(content, "(空)") + "\n\n"
                + (materialText.isBlank() ? "" : "材料/语境（节选）：\n" + materialText + "\n\n")
                + "官方高分范文：\n" + defaultText(correctAnswer, "(未提供官方范文，你自己给一版高分参考)") + "\n\n"
                + "学生的作答：\n" + defaultText(userAnswer, "(未作答)") + "\n\n"
                + "请按这个结构来（人话口吻，别机器味）：\n\n"
                + "1. 【直击痛点】\n用一两句话直接评价整体水平（别绕弯子）。指出 1-2 个最致命的语法/拼写/表达问题，并把正确写法直接甩出来。\n\n"
                + "2. 【范文降维打击】\n别拆解一堆知识点。挑范文里最值得“抄作业”的 1-2 个高级词/神仙句型/连接词，用大白话说清楚：人家为什么这么写就显得地道。\n\n"
                + "3. 【老学长的建议】\n结合你的作答，给 3 条具体可执行的改进建议（要接地气，比如“下次别老用 think，试试用…… ”）。\n\n"
                + "4. 【给你一版能直接交的改写】\n写作：给一版更高分的改写范文。\n翻译：给一版更地道的完整参考译文。\n\n"
                + "注意：\n- 多用空行，排版清爽\n- 绝对别出现“希望这能帮到你”这种收尾\n- 用 Markdown 输出（比如用小标题、列表）";
        }

        return "你是一个幽默、接地气但讲题很清楚的英语辅导学长。\n\n"
            + "硬性要求：\n"
            + "- 开头第一句直接给结论（对在哪/错在哪）\n"
            + "- 无论答对还是答错，都要对比“用户答案 vs 正确答案”\n"
            + "- 选择题要逐选项解释为什么对/错\n"
            + "- 输出中文，用 Markdown 排版，空行多一点\n"
            + "- 严禁套话：首先/其次/最后/总而言之/值得注意的是/希望对你有帮助\n\n"
            + "现在有一道题：\n"
            + "所属部分：" + defaultText(header, "(未知)") + "\n\n"
            + "题目原文：\n" + defaultText(content, "(空)") + "\n\n"
            + (optionsText.isBlank() ? "" : "选项：\n" + optionsText + "\n\n")
            + "正确答案：" + defaultText(correctAnswer, "(无)") + "\n"
            + "同学作答：" + defaultText(userAnswer, "(未作答)") + "\n"
            + (explanation.isBlank() ? "" : "原解析参考（别照抄）：" + explanation + "\n")
            + (materialText.isBlank() ? "" : "\n语境/材料（节选）：\n" + materialText + "\n")
            + (materialImage.isBlank() ? "" : "材料图片：" + materialImage + "\n")
            + (audioUrl.isBlank() ? "" : "听力音频：" + audioUrl + "\n")
            + "请用大白话解释清楚：\n"
            + "- 这题最经典的坑/陷阱到底在哪\n"
            + "- 同学这份作答为什么对 / 为什么不对（不管答对答错，都要对比“他写了啥 vs 正确答案为什么是这个”）\n"
            + "- 选择题的话，顺便吐槽一下其他选项为什么是坑\n\n"
            + "要求：\n"
            + "1. 坚决别用“首先/其次/最后/总而言之/值得注意的是/希望对你有帮助”等套话\n"
            + "2. 别背书，别按知识点列清单，直接一针见血\n"
            + "3. 语气像朋友聊天，可以带点幽默/吐槽（比如“这题是经典坑”）\n"
            + "4. 开头第一句直接给结论（他错在哪/对在哪）\n"
            + "5. 结尾用一句话给个记忆小诀窍\n\n"
            + "用 Markdown 排版，空行多一点。";
    }

    private boolean isWritingType(String itemType) {
        if (itemType == null) {
            return false;
        }
        String normalized = itemType.trim().toLowerCase();
        return "essay".equals(normalized)
            || "writing".equals(normalized)
            || "translation".equals(normalized)
            || "speaking".equals(normalized);
    }

    private String formatOptions(Object options) {
        if (options == null) {
            return "";
        }

        if (options instanceof List<?> list) {
            StringBuilder builder = new StringBuilder();
            for (Object item : list) {
                Map<String, Object> option = asMap(item);
                String key = toText(option.get("key"));
                String value = toText(option.get("value"));
                if (key.isBlank() && value.isBlank()) {
                    continue;
                }
                if (builder.length() > 0) {
                    builder.append('\n');
                }
                builder.append(defaultText(key, "?")).append(". ").append(defaultText(value, ""));
            }
            return builder.toString();
        }

        return toText(options);
    }

    private String joinNonEmpty(String separator, String... parts) {
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (part == null || part.isBlank()) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(separator);
            }
            builder.append(part.trim());
        }
        return builder.toString();
    }

    private String defaultText(String text, String fallback) {
        return text == null || text.isBlank() ? fallback : text;
    }

    private String stringify(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof List<?> list) {
            return list.stream().map(this::stringify).filter(s -> !s.isBlank()).reduce((a, b) -> a + ", " + b).orElse("");
        }
        if (value instanceof Map<?, ?> map) {
            return map.toString();
        }
        return String.valueOf(value);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> asMap(Object value) {
        if (value instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        return new HashMap<>();
    }

    private String toText(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }
}
