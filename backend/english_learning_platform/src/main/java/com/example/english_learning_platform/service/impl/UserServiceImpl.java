// com/example/english_learning_platform/service/impl/UserServiceImpl.java
package com.example.english_learning_platform.service.impl;

import com.example.english_learning_platform.dto.*;
import com.example.english_learning_platform.entity.User;
import com.example.english_learning_platform.repository.UserRepository;
import com.example.english_learning_platform.service.UserService;
import com.example.english_learning_platform.util.JwtUtil;
import com.example.english_learning_platform.vo.LoginVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public LoginVO login(LoginDTO loginDTO) {
        // 查找用户
        User user = userRepository.findByUsername(loginDTO.getUsername())
                .orElseThrow(() -> new RuntimeException("用户名不存在"));

        // 验证密码
        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new RuntimeException("密码错误");
        }


        userRepository.save(user);

        // 生成token
        String token = jwtUtil.generateToken(user.getId());

        // 返回登录信息
        LoginVO loginVO = new LoginVO();
        loginVO.setToken(token);
        loginVO.setUserInfo(user);
        return loginVO;
    }

    @Override
    public User register(RegisterDTO registerDTO) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(registerDTO.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }

        // 创建新用户
        User user = new User();
        user.setUsername(registerDTO.getUsername());
        // 加密密码
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
//        user.setEmail(registerDTO.getEmail());
//        user.setPhone(registerDTO.getPhone());
//输入邮箱和手机号暂时不实现
        // 保存用户
        return userRepository.save(user);
    }

    @Override
    public void logout(Long userId) {
        // 实际项目中可以在这里实现token黑名单等逻辑
    }

    // 其他方法实现...
    @Override
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }

    @Override
    public String sendVerifyCode(String account) {
        // 实现发送验证码逻辑
        return "verify_code";
    }

    @Override
    public boolean resetPassword(PasswordResetDTO resetDTO) {
        // 实现重置密码逻辑
        return true;
    }

    @Override
    public User updateUserInfo(Long userId, UserUpdateDTO updateDTO) {
        User user = getUserById(userId);
        // 更新用户信息
        return userRepository.save(user);
    }

    @Override
    public boolean updatePassword(Long userId, PasswordUpdateDTO passwordDTO) {
        // 实现更新密码逻辑
        return true;
    }
}