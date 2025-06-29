// --- 檔案：security/JwtRequestFilter.java ---
// 一個 Spring Security 過濾器，攔截所有請求並驗證 JWT。

package com.taskproject.attachmentapi.security;

import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        String jwt = null;
        Long userId = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwt = authHeader.substring(7);
            try {
                // 從 Token 中解析出 userId
                userId = jwtUtil.extractUserId(jwt);
            } catch (ExpiredJwtException e) {
                // Token 過期，後面會被拒絕
            } catch (Exception e) {
                // Token 解析失敗
            }
        }

        // 如果成功解析出 userId 且當前的 SecurityContext 是空的
        if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // 驗證 token 是否有效
            if (jwtUtil.validateToken(jwt)) {
                // 建立一個認證成功的 Token。
                // 我們將 userId 設為 Principal，這樣在 Controller 中就能直接取得。
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userId, null, Collections.emptyList());
                
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                
                // 將這個認證成功的 Token 放入 Spring Security 的上下文中
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        
        // 繼續執行後續的過濾器鏈
        filterChain.doFilter(request, response);
    }
}