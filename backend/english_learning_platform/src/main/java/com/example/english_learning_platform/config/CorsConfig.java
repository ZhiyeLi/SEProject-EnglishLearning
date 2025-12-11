// com/example/english_learning_platform/config/CorsConfig.java
package com.example.english_learning_platform.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOriginPattern("*");  // 允许所有源
        config.addAllowedHeader("*");         // 允许所有头
        config.addAllowedMethod("*");         // 允许所有方法
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}