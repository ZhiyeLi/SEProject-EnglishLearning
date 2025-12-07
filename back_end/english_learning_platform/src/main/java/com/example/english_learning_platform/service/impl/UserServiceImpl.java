package com.example.english_learning_platform.service.impl;

import com.example.english_learning_platform.dto.*;
import com.example.english_learning_platform.entity.User;
import com.example.english_learning_platform.repository.UserRepository;
import com.example.english_learning_platform.service.UserService;
import com.example.english_learning_platform.util.JwtUtil;
import com.example.english_learning_platform.vo.LoginVO;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    // 临时存储验证码（生产环境建议用Redis，设置过期时间）
    private final Map<String, String> verifyCodeCache = new HashMap<>();

    /**
     * 登录逻辑
     */
    @Override
    public LoginVO login(LoginDTO loginDTO) {
        // 1. 查询用户
        User user = userRepository.findByUsername(loginDTO.getUsername())
                .orElseThrow(() -> new RuntimeException("用户名或密码错误"));

        // 2. 验证密码（BCrypt加密匹配）
        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 3. 生成JWT令牌
        String token = jwtUtil.generateToken(user.getId());

        // 4. 封装返回结果
        LoginVO loginVO = new LoginVO();
        loginVO.setToken(token);
        loginVO.setUserInfo(user);
        return loginVO;
    }

    /**
     * 注册逻辑
     */
    @Override
    public User register(RegisterDTO registerDTO) {
        // 1. 校验用户名是否已存在
        Optional<User> existingUser = userRepository.findByUsername(registerDTO.getUsername());
        if (existingUser.isPresent()) {
            throw new RuntimeException("用户名已存在");
        }

        // 2. 构建用户对象
        User user = new User();
        user.setUsername(registerDTO.getUsername());
        // 密码加密存储
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        user.setEmail(registerDTO.getEmail());
        user.setPhone(registerDTO.getPhone());
        // 默认值
        user.setAvatar("https://picsum.photos/seed/" + registerDTO.getUsername() + "/100/100");
        user.setJoinTime(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        user.setSignature("暂无个性签名");
        user.setLocation("未设置");

        // 3. 保存用户
        return userRepository.save(user);
    }

    /**
     * 根据ID获取用户信息
     */
    @Override
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("用户不存在"));
    }

    /**
     * 更新用户基本信息
     */
    @Override
    public User updateUserInfo(Long userId, UserUpdateDTO updateDTO) {
        // 1. 查询用户
        User user = getUserById(userId);

        // 2. 覆盖可更新字段（非空才更新）
        if (StringUtils.hasText(updateDTO.getAvatar())) {
            user.setAvatar(updateDTO.getAvatar());
        }
        if (StringUtils.hasText(updateDTO.getSignature())) {
            user.setSignature(updateDTO.getSignature());
        }
        if (StringUtils.hasText(updateDTO.getLocation())) {
            user.setLocation(updateDTO.getLocation());
        }
        if (StringUtils.hasText(updateDTO.getEmail())) {
            user.setEmail(updateDTO.getEmail());
        }
        if (StringUtils.hasText(updateDTO.getPhone())) {
            user.setPhone(updateDTO.getPhone());
        }

        // 3. 保存更新
        return userRepository.save(user);
    }

    /**
     * 修改密码（已登录状态，验证旧密码）
     */
    @Override
    public boolean updatePassword(Long userId, PasswordUpdateDTO passwordDTO) {
        // 1. 前端参数二次校验
        if (!passwordDTO.getNewPassword().equals(passwordDTO.getConfirmNewPassword())) {
            throw new RuntimeException("两次输入的新密码不一致");
        }
        if (passwordDTO.getOldPassword().equals(passwordDTO.getNewPassword())) {
            throw new RuntimeException("新密码不能与旧密码相同");
        }

        // 2. 查询用户
        User user = getUserById(userId);

        // 3. 验证旧密码
        if (!passwordEncoder.matches(passwordDTO.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("旧密码输入错误");
        }

        // 4. 加密新密码并更新
        user.setPassword(passwordEncoder.encode(passwordDTO.getNewPassword()));
        userRepository.save(user);
        return true;
    }

    /**
     * 发送验证码（重置密码用）
     */
    @Override
    public String sendVerifyCode(String account) {
        // 1. 校验账号是否存在（手机号/邮箱匹配用户）
        Optional<User> userOpt = userRepository.findByUsername(account)
                .or(() -> userRepository.findByPhone(account)
                        .or(() -> userRepository.findByEmail(account)));
        if (userOpt.isEmpty()) {
            throw new RuntimeException("该账号不存在");
        }

        // 2. 生成6位数字验证码
        String verifyCode = String.valueOf(new Random().nextInt(899999) + 100000);

        // 3. 存储验证码（生产环境建议用Redis，设置5分钟过期）
        verifyCodeCache.put(account, verifyCode);

        // 4. 模拟发送验证码（生产环境需对接短信/邮件服务商）
        System.out.println("向账号[" + account + "]发送验证码：" + verifyCode);

        return verifyCode; // 仅测试用，生产环境不返回验证码
    }

    /**
     * 重置密码（忘记密码，通过验证码）
     */
    @Override
    public boolean resetPassword(PasswordResetDTO resetDTO) {
        // 1. 校验验证码
        String cachedCode = verifyCodeCache.get(resetDTO.getAccount());
        if (cachedCode == null || !cachedCode.equals(resetDTO.getVerifyCode())) {
            throw new RuntimeException("验证码错误或已过期");
        }

        // 2. 查询用户
        User user = userRepository.findByUsername(resetDTO.getAccount())
                .or(() -> userRepository.findByPhone(resetDTO.getAccount())
                        .or(() -> userRepository.findByEmail(resetDTO.getAccount())))
                .orElseThrow(() -> new RuntimeException("账号不存在"));

        // 3. 更新密码
        user.setPassword(passwordEncoder.encode(resetDTO.getNewPassword()));
        userRepository.save(user);

        // 4. 清空验证码
        verifyCodeCache.remove(resetDTO.getAccount());
        return true;
    }

    /**
     * 登出（后端仅标记token失效，前端清除localStorage）
     */
    @Override
    public void logout(Long userId) {
        // 生产环境需实现token黑名单（如Redis存储失效token）
        System.out.println("用户[" + userId + "]已登出");
    }
}