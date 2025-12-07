package com.example.english_learning_platform.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PasswordResetDTO {
    @NotBlank(message = "账号（手机号/邮箱）不能为空")
    private String account;

    @NotBlank(message = "验证码不能为空")
    private String verifyCode;

    @NotBlank(message = "新密码不能为空")
    private String newPassword;
}