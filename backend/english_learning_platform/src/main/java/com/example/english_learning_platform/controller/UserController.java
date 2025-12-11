package com.example.english_learning_platform.controller;

import com.example.english_learning_platform.dto.PasswordUpdateDTO;
import com.example.english_learning_platform.dto.UserUpdateDTO;
import com.example.english_learning_platform.entity.User;
import com.example.english_learning_platform.service.UserService;
import com.example.english_learning_platform.util.JwtUtil;
import com.example.english_learning_platform.util.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user") // 统一前缀为 /api/user，避免与 AuthController 冲突
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    // 获取当前登录用户信息
    @GetMapping("/info")
    public Result getUserInfo(@RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.getUserIdFromToken(token.replace("Bearer ", ""));
        User user = userService.getUserById(userId);
        return Result.success(user);
    }

    // 更新用户基本信息
    @PutMapping("/info")
    public Result updateUserInfo(
            @RequestBody UserUpdateDTO updateDTO,
            @RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.getUserIdFromToken(token.replace("Bearer ", ""));
        User updatedUser = userService.updateUserInfo(userId, updateDTO);
        return Result.success(updatedUser);
    }

    // 修改密码
    @PutMapping("/password")
    public Result updatePassword(
            @Valid @RequestBody PasswordUpdateDTO passwordDTO,
            @RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.getUserIdFromToken(token.replace("Bearer ", ""));
        boolean success = userService.updatePassword(userId, passwordDTO);
        return success ? Result.success("密码修改成功") : Result.error("密码修改失败");
    }

    // 登出
    @PostMapping("/logout")
    public Result logout(@RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.getUserIdFromToken(token.replace("Bearer ", ""));
        userService.logout(userId);
        return Result.success("登出成功");
    }
}