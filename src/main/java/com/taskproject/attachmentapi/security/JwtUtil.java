// --- 檔案：security/JwtUtil.java ---
// 一個工具類，專門用來產生、解析和驗證 JWT。

package com.taskproject.attachmentapi.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;

@Component
public class JwtUtil {

    // 從 application.properties 讀取 JWT 密鑰
    @Value("${jwt.secret}")
    private String secret;

    // 根據 secret 字串產生一個安全的簽章金鑰
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
    
    // 從 Token 中解析出使用者名稱 (email)
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    
    // 從 Token 中解析出使用者 ID
    public Long extractUserId(String token) {
        final Claims claims = extractAllClaims(token);
        // 我們假設 TaskApi 在產生 token 時，將 userId 存在名為 "id" 的 claim 中
        return claims.get("id", Long.class);
    }

    // 驗證 Token 是否有效且未過期
    public boolean validateToken(String token) {
        return !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        // 使用與產生 Token 時相同的金鑰來解析
        return Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token).getBody();
    }
}