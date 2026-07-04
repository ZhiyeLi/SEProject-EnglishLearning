package com.example.english_learning_platform.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 4, max = 20, message = "用户名需为4-20位字符")
    private String userName;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度需在6-20位之间")
    private String userPassword;

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String userEmail;
}
