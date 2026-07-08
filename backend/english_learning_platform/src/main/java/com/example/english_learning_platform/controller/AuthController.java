package com.example.english_learning_platform.controller;

import com.example.english_learning_platform.dto.ApiResponse;
import com.example.english_learning_platform.dto.LoginRequest;
import com.example.english_learning_platform.dto.RegisterRequest;
import com.example.english_learning_platform.dto.UserDTO;
import com.example.english_learning_platform.service.AuthService;
import com.example.english_learning_platform.service.IpDefenseService;
import com.example.english_learning_platform.service.RecaptchaService;
import jakarta.validation.Valid;
import com.example.english_learning_platform.entity.User;
import com.example.english_learning_platform.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;
    private final IpDefenseService ipDefenseService;
    private final RecaptchaService recaptchaService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(
            AuthService authService,
            IpDefenseService ipDefenseService,
            RecaptchaService recaptchaService,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.authService = authService;
        this.ipDefenseService = ipDefenseService;
        this.recaptchaService = recaptchaService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    @PostMapping("/register")
    public ApiResponse<Map<String, Object>> register(@Valid @RequestBody RegisterRequest request, HttpServletRequest httpRequest) {
        try {
            String ip = getClientIp(httpRequest);
            boolean requireCaptcha = ipDefenseService.recordAttemptAndCheck("register", ip);
            if (requireCaptcha) {
                RecaptchaService.RecaptchaResult verifyResult = recaptchaService.verify(request.getRecaptchaToken(), ip);
                if (!verifyResult.isOk()) {
                    return ApiResponse.success(verifyResult.getMessage(), Map.of("requireCaptcha", true));
                }
            }

            Map<String, Object> result = authService.register(request);
            ipDefenseService.clearAttempts("register", ip);
            return ApiResponse.success(result);
        } catch (Exception e) {
            return ApiResponse.badRequest(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ApiResponse<Map<String, Object>> login(@RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        try {
            String ip = getClientIp(httpRequest);
            boolean requireCaptcha = ipDefenseService.recordAttemptAndCheck("login", ip);
            if (requireCaptcha) {
                RecaptchaService.RecaptchaResult verifyResult = recaptchaService.verify(request.getRecaptchaToken(), ip);
                if (!verifyResult.isOk()) {
                    return ApiResponse.success(verifyResult.getMessage(), Map.of("requireCaptcha", true));
                }
            }

            Map<String, Object> result = authService.login(request);
            ipDefenseService.clearAttempts("login", ip);
            return ApiResponse.success(result);
        } catch (Exception e) {
            return ApiResponse.unauthorized(e.getMessage());
        }
    }

    @GetMapping("/user")
    public ApiResponse<UserDTO> getCurrentUser(HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            UserDTO user = authService.getUserInfo(userId);
            return ApiResponse.success(user);
        } catch (Exception e) {
            return ApiResponse.notFound(e.getMessage());
        }
    }

    @PutMapping("/user")
    public ApiResponse<UserDTO> updateUserInfo(HttpServletRequest request, @RequestBody User updates) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            UserDTO user = authService.updateUserInfo(userId, updates);
            return ApiResponse.success(user);
        } catch (Exception e) {
            return ApiResponse.notFound(e.getMessage());
        }
    }

    @PostMapping("/change-password")
    public ApiResponse<String> changePassword(HttpServletRequest request, @RequestBody Map<String, String> data) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            String oldPassword = data.get("oldPassword");
            String newPassword = data.get("newPassword");
            authService.changePassword(userId, oldPassword, newPassword);
            return ApiResponse.success("密码修改成功");
        } catch (Exception e) {
            return ApiResponse.badRequest(e.getMessage());
        }
    }

    @PostMapping("/verify-password")
    public ApiResponse<String> verifyPassword(HttpServletRequest request, @RequestBody Map<String, String> data) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            String password = data.get("password");
            authService.verifyPassword(userId, password);
            return ApiResponse.success("密码验证成功");
        } catch (Exception e) {
            return ApiResponse.badRequest(e.getMessage());
        }
    }

    @PostMapping("/send-verify-code")
    public ApiResponse<String> sendVerifyCode(@RequestBody Map<String, String> body) {
        try {
            String account = body.get("account");
            String type = body.get("type");

            if (account == null || account.isBlank()) {
                return ApiResponse.badRequest("账号不能为空");
            }
            if (type == null || (!type.equals("email") && !type.equals("phone"))) {
                return ApiResponse.badRequest("类型必须为 email 或 phone");
            }

            boolean exists;
            if ("email".equals(type)) {
                exists = userRepository.findByUserEmail(account).isPresent();
            } else {
                exists = userRepository.findByUserName(account).isPresent();
            }
            if (!exists) {
                return ApiResponse.notFound("账号不存在");
            }

            logger.info("Verification code sent to {} via {}", account, type);
            return ApiResponse.success("验证码发送成功");
        } catch (Exception e) {
            logger.error("Failed to send verification code", e);
            return ApiResponse.error("验证码发送失败");
        }
    }

    @PostMapping("/reset-password")
    public ApiResponse<String> resetPassword(@RequestBody Map<String, String> body) {
        try {
            String account = body.get("account");
            String type = body.get("type");
            String verifyCode = body.get("verifyCode");
            String newPassword = body.get("newPassword");

            if (account == null || account.isBlank()) {
                return ApiResponse.badRequest("账号不能为空");
            }
            if (type == null || (!type.equals("email") && !type.equals("phone"))) {
                return ApiResponse.badRequest("类型必须为 email 或 phone");
            }
            if (verifyCode == null || verifyCode.isBlank()) {
                return ApiResponse.badRequest("验证码不能为空");
            }
            if (newPassword == null || newPassword.length() < 6) {
                return ApiResponse.badRequest("密码长度不能少于6位");
            }
            if (verifyCode.length() != 6) {
                return ApiResponse.badRequest("验证码格式不正确");
            }

            User user;
            if ("email".equals(type)) {
                user = userRepository.findByUserEmail(account).orElse(null);
            } else {
                user = userRepository.findByUserName(account).orElse(null);
            }
            if (user == null) {
                return ApiResponse.notFound("账号不存在");
            }

            user.setUserPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);

            logger.info("Password reset successfully for user {}", user.getUserId());
            return ApiResponse.success("密码重置成功");
        } catch (Exception e) {
            logger.error("Failed to reset password", e);
            return ApiResponse.error("密码重置失败");
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return normalizeIp(forwarded.split(",")[0].trim());
        }
        return normalizeIp(request.getRemoteAddr());
    }

    private String normalizeIp(String ip) {
        if (ip == null) {
            return "unknown";
        }
        if (ip.startsWith("::ffff:")) {
            return ip.substring(7);
        }
        return ip;
    }
}
