package com.example.english_learning_platform.service;

import com.example.english_learning_platform.dto.*;
import com.example.english_learning_platform.entity.User;
import com.example.english_learning_platform.vo.LoginVO;

public interface UserService {
    // 登录
    LoginVO login(LoginDTO loginDTO);

    // 注册
    User register(RegisterDTO registerDTO);

    // 根据ID获取用户信息
    User getUserById(Long userId);

    // 更新用户基本信息（头像、签名、所在地等）
    User updateUserInfo(Long userId, UserUpdateDTO updateDTO);

    // 修改密码（已登录状态，需验证旧密码）
    boolean updatePassword(Long userId, PasswordUpdateDTO passwordDTO);

    // 发送验证码（重置密码用）
    String sendVerifyCode(String account); // account: 手机号/邮箱

    // 重置密码（忘记密码，通过验证码）
    boolean resetPassword(PasswordResetDTO resetDTO);

    // 登出（销毁token，前端配合清除localStorage）
    void logout(Long userId);
}