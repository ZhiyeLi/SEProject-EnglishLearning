package com.example.english_learning_platform.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class RecaptchaService {

        private static final String[] VERIFY_URLS = new String[] {
            "https://www.recaptcha.net/recaptcha/api/siteverify",
            "https://www.google.com/recaptcha/api/siteverify"
        };

    private final RestTemplate restTemplate;

    @Value("${recaptcha.secret:}")
    private String secret;

    public RecaptchaService(RestTemplateBuilder builder) {
        this.restTemplate = builder.build();
    }

    public RecaptchaResult verify(String token, String remoteIp) {
        if (secret == null || secret.isBlank()) {
            return RecaptchaResult.fail("reCaptcha 未配置");
        }
        if (token == null || token.isBlank()) {
            return RecaptchaResult.fail("缺少验证码");
        }

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("secret", secret);
        form.add("response", token);
        if (remoteIp != null && !remoteIp.isBlank()) {
            form.add("remoteip", remoteIp);
        }

        Exception lastError = null;
        for (String url : VERIFY_URLS) {
            try {
                Map<?, ?> response = restTemplate.postForObject(url, form, Map.class);
                if (response != null && Boolean.TRUE.equals(response.get("success"))) {
                    return RecaptchaResult.ok();
                }
                return RecaptchaResult.fail("验证码校验失败");
            } catch (Exception ex) {
                lastError = ex;
            }
        }

        return RecaptchaResult.fail("验证码服务不可用");
    }

    public static class RecaptchaResult {
        private final boolean ok;
        private final String message;

        private RecaptchaResult(boolean ok, String message) {
            this.ok = ok;
            this.message = message;
        }

        public static RecaptchaResult ok() {
            return new RecaptchaResult(true, "");
        }

        public static RecaptchaResult fail(String message) {
            return new RecaptchaResult(false, message);
        }

        public boolean isOk() {
            return ok;
        }

        public String getMessage() {
            return message;
        }
    }
}
