package com.example.english_learning_platform.controller;

import com.example.english_learning_platform.dto.LoginDTO;
import com.example.english_learning_platform.dto.PasswordResetDTO;
import com.example.english_learning_platform.dto.RegisterDTO;
import com.example.english_learning_platform.entity.User;
import com.example.english_learning_platform.service.UserService;
import com.example.english_learning_platform.util.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth") // 统一前缀为 /api/auth
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    // 登录（仅保留这一个 /api/auth/login）
    @PostMapping("/login")
    public Result login(@Valid @RequestBody LoginDTO loginDTO) {
        return Result.success(userService.login(loginDTO));
    }

    // 注册
    @PostMapping("/register")
    public Result register(@Valid @RequestBody RegisterDTO registerDTO) {
        User user = userService.register(registerDTO);
        return Result.success(user);
    }

    // 发送验证码
    @PostMapping("/verify-code")
    public Result sendVerifyCode(@RequestParam String account) {
        userService.sendVerifyCode(account);
        return Result.success("验证码发送成功");
    }

    // 重置密码
    @PostMapping("/reset-password")
    public Result resetPassword(@Valid @RequestBody PasswordResetDTO resetDTO) {
        boolean success = userService.resetPassword(resetDTO);
        return success ? Result.success("密码重置成功") : Result.error("密码重置失败");
    }
}