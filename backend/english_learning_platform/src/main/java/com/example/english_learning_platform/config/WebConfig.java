package com.example.english_learning_platform.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web 配置类
 * 配置静态资源映射和跨域设置
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload.base-path}")
    private String basePath;

    /**
     * 配置静态资源映射
     * 将 /api/files/** 的请求映射到本地文件系统
     */
    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        // 映射音频文件
        registry.addResourceHandler("/api/files/audio/**")
                .addResourceLocations("file:" + basePath + "/audio/")
                .setCachePeriod(3600); // 缓存 1 小时

        // 映射图片文件
        registry.addResourceHandler("/api/files/images/**")
                .addResourceLocations("file:" + basePath + "/images/")
                .setCachePeriod(3600); // 缓存 1 小时

        // 通用映射（可选，作为备用）
        registry.addResourceHandler("/api/files/**")
                .addResourceLocations("file:" + basePath + "/")
                .setCachePeriod(3600);
    }

    /**
     * 配置跨域访问
     */
    @Override
    public void addCorsMappings(@NonNull CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(false)
                .maxAge(3600);
    }
}
