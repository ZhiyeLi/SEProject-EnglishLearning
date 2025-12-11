package com.example.english_learning_platform.controller;

import com.example.english_learning_platform.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 测试接口控制器（仅用于调试，生产环境可删除/禁用）
 * 用于验证数据库连接、接口连通性等
 */
@RestController
@RequestMapping("/api/test")  // 统一前缀，与核心接口区分
public class TestController {

    // 注入用户仓库，测试数据库交互
    @Autowired
    private UserRepository userRepository;

    /**
     * 测试后端是否能正常连接数据库
     * 访问地址：http://localhost:8080/api/test/db
     */
    @GetMapping("/db")
    public String testDatabaseConnection() {
        try {
            // 尝试查询用户表总数（最简单的数据库交互）
            long userCount = userRepository.count();
            return String.format("✅ 数据库连接成功！\n用户表总记录数：%d", userCount);
        } catch (Exception e) {
            // 捕获所有数据库相关异常，返回具体错误信息
            return String.format("❌ 数据库连接失败！\n错误原因：%s", e.getMessage());
        }
    }

    /**
     * 测试前端-后端是否能正常连通（无数据库交互）
     * 访问地址：http://localhost:8080/api/test/ping
     */
    @GetMapping("/ping")
    public String testFrontendBackendConnection() {
        return "✅ 前端-后端连接正常！当前时间：" + System.currentTimeMillis();
    }

    /**
     * 测试登录核心逻辑（可选，模拟登录）
     * 访问地址：http://localhost:8080/api/test/login-demo?username=test&password=123456
     */
    @GetMapping("/login-demo")
    public String testLoginLogic(String username, String password) {
        try {
            // 模拟登录流程：查询用户 → 验证密码（简化版）
            boolean userExists = userRepository.existsByUsername(username);
            if (!userExists) {
                return "❌ 模拟登录失败：用户名不存在";
            }
            // 此处仅验证用户存在，实际密码验证需调用UserService
            return String.format("✅ 模拟登录成功：用户名%s存在（密码验证需调用UserService）", username);
        } catch (Exception e) {
            return String.format("❌ 模拟登录失败：%s", e.getMessage());
        }
    }
}