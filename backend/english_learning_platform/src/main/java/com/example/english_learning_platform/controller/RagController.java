package com.example.english_learning_platform.controller;

import com.example.english_learning_platform.dto.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
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
}
