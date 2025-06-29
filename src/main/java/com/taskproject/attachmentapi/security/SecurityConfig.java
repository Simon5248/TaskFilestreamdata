// --- 檔案：security/SecurityConfig.java ---
// Spring Security 的主要設定檔 (適用於 Spring Boot 2.7.x)。

package com.taskproject.attachmentapi.security;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtRequestFilter jwtRequestFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // 因為我們是 REST API，不需要 CSRF 保護
                .csrf().disable()
                // 設定所有 API 請求都需要被認證
                .authorizeRequests()
                .antMatchers("/api/**").authenticated()
                .anyRequest().permitAll() // 如果有其他非 /api 的路徑，例如健康檢查，可以允許
                .and()
                // 設定 Session 管理為 STATELESS，因為我們用 JWT，伺服器不需要儲存 session
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                // 在處理使用者名稱密碼認證的過濾器之前，先加上我們的 JWT 過濾器
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

// 2. 定義 CORS 的具體規則，這是一個必須的 Bean
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // 允許的來源 (您的前端)
        configuration.setAllowedOrigins(Arrays.asList("http://127.0.0.1:5500","http://192.168.190.131:5500","http://192.168.112.16:7380","http://192.168.112.16:7381","http://192.168.112.16:8380"));
        // 允許的方法
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        // 允許的標頭
        configuration.setAllowedHeaders(Arrays.asList("*"));
        // 允許傳遞憑證 (如 cookies)
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // 對所有路徑套用此規則
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

}