package com.example.english_learning_platform.service;

import com.example.english_learning_platform.dto.LoginRequest;
import com.example.english_learning_platform.dto.RegisterRequest;
import com.example.english_learning_platform.dto.UserDTO;
import com.example.english_learning_platform.entity.User;
import com.example.english_learning_platform.repository.UserRepository;
import com.example.english_learning_platform.util.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    
    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
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
        
        if (updates.getUserEmail() != null) {
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
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        if (!passwordEncoder.matches(oldPassword, user.getUserPassword())) {
            throw new RuntimeException("原密码错误");
        }
        
        user.setUserPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
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
