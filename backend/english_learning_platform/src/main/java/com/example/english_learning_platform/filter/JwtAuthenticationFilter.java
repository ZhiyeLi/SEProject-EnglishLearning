package com.example.english_learning_platform.filter;

import com.example.english_learning_platform.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        // 1. 无合法 Bearer 格式直接放行，不执行后续校验
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        try {
            // 2. token 校验通过，且当前上下文无认证信息时才设置
            if (jwtUtil.validateToken(token)
                    && SecurityContextHolder.getContext().getAuthentication() == null) {

                Long userId = jwtUtil.getUserIdFromToken(token);
                String userName = jwtUtil.getUserNameFromToken(token);

                // 3. 权限/角色配置（基础示例：默认普通用户角色）
                // 后续扩展：从token payload解析角色 / 根据userId查数据库获取权限列表
                List<GrantedAuthority> authorities = AuthorityUtils.createAuthorityList("ROLE_USER");

                // 4. 构建认证对象，存入Spring Security上下文
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userId, null, authorities);
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);

                // 5. 存入request属性，Controller层可直接通过@RequestAttribute取用
                request.setAttribute("userId", userId);
                request.setAttribute("userName", userName);
            }
        } catch (Exception e) {
            // 兜底捕获所有token异常：过期、签名错误、格式非法、token篡改等
            // 静默处理，不设置认证，请求继续走过滤器链，由权限规则最终拦截
            log.warn("JWT token校验失败: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}