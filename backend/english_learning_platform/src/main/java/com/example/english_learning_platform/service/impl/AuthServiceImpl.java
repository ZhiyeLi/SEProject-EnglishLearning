package com.example.english_learning_platform.service.impl;

import com.example.english_learning_platform.dto.LoginRequest;
import com.example.english_learning_platform.dto.RegisterRequest;
import com.example.english_learning_platform.dto.UserDTO;
import com.example.english_learning_platform.entity.User;
import com.example.english_learning_platform.repository.UserRepository;
import com.example.english_learning_platform.service.AuthService;
import com.example.english_learning_platform.util.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthServiceImpl implements AuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    
    public AuthServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }
    
    @Transactional
    public Map<String, Object> register(RegisterRequest request) {
        // 检查用户名是否已存在
        if (userRepository.findByUserName(request.getUserName()).isPresent()) {
            throw new RuntimeException("用户名已存在");
        }
        
        // 检查邮箱是否已存在
        if (userRepository.findByUserEmail(request.getUserEmail()).isPresent()) {
            throw new RuntimeException("邮箱已被使用");
        }
        
        // 创建新用户
        User user = new User();
        user.setUserName(request.getUserName());
        user.setUserPassword(passwordEncoder.encode(request.getUserPassword()));
        user.setUserEmail(request.getUserEmail());
        
        user = userRepository.save(user);
        
        // 生成token
        String token = jwtUtil.generateToken(user.getUserId(), user.getUserName());
        
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", convertToDTO(user));
        
        return result;
    }
    
    public Map<String, Object> login(LoginRequest request) {
        User user = userRepository.findByUserName(request.getUserName())
                .orElseThrow(() -> new RuntimeException("用户名或密码错误"));
        
        if (!passwordEncoder.matches(request.getUserPassword(), user.getUserPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }
        
        // 生成token
        String token = jwtUtil.generateToken(user.getUserId(), user.getUserName());
        
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", convertToDTO(user));
        
        return result;
    }
    
    public UserDTO getUserInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        return convertToDTO(user);
    }
    
    @Transactional
    public UserDTO updateUserInfo(Long userId, User updates) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        if (updates.getUserName() != null && !updates.getUserName().isBlank()) {
            userRepository.findByUserName(updates.getUserName()).ifPresent(existing -> {
                if (!existing.getUserId().equals(userId)) {
                    throw new RuntimeException("用户名已被使用");
                }
            });
            user.setUserName(updates.getUserName());
        }
        if (updates.getUserEmail() != null && !updates.getUserEmail().isBlank()) {
            user.setUserEmail(updates.getUserEmail());
        }
        if (updates.getAvatar() != null) {
            user.setAvatar(updates.getAvatar());
        }
        if (updates.getUserStatus() != null) {
            user.setUserStatus(updates.getUserStatus());
        }
        if (updates.getSignature() != null) {
            user.setSignature(updates.getSignature());
        }

        user = userRepository.save(user);
        return convertToDTO(user);
    }
    
    @Transactional
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        if (oldPassword == null || oldPassword.isBlank()) {
            throw new RuntimeException("原密码不能为空");
        }
        if (newPassword == null || newPassword.isBlank()) {
            throw new RuntimeException("新密码不能为空");
        }
        if (newPassword.length() < 6) {
            throw new RuntimeException("新密码长度不能少于6位");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        if (!passwordEncoder.matches(oldPassword, user.getUserPassword())) {
            throw new RuntimeException("原密码错误");
        }

        user.setUserPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
    
    public void verifyPassword(Long userId, String password) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        if (!passwordEncoder.matches(password, user.getUserPassword())) {
            throw new RuntimeException("密码错误");
        }
    }
    
    private UserDTO convertToDTO(User user) {
        String createdAtStr = user.getCreatedAt() != null ? 
            user.getCreatedAt().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")) : 
            java.time.LocalDate.now().toString();
            
        return new UserDTO(
            user.getUserId(),
            user.getUserName(),
            user.getUserEmail(),
            user.getAvatar(),
            user.getUserStatus(),
            user.getSignature(),
            user.getStreak(),
            createdAtStr
        );
    }
}
