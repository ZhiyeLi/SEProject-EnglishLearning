package com.example.english_learning_platform.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {
    
    // 使用固定的密钥，确保重启后Token依然有效 (至少32个字符)
    private static final String SECRET_STRING = "MySuperSecretKeyForEnglishLearningPlatform2025";
    private static final Key SECRET_KEY = Keys.hmacShaKeyFor(SECRET_STRING.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    private static final long EXPIRATION_TIME = 86400000; // 24小时
    
    /**
     * 生成JWT Token
     */
    public String generateToken(Long userId, String userName) {
        return Jwts.builder()
                .setSubject(userId.toString())
                .claim("userName", userName)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SECRET_KEY)
                .compact();
    }
    
    /**
     * 从Token中获取用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return Long.parseLong(claims.getSubject());
    }
    
    /**
     * 从Token中获取用户名
     */
    public String getUserNameFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get("userName", String.class);
    }
    
    /**
     * 验证Token是否有效
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 从Token中解析Claims
     */
    private Claims getClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
