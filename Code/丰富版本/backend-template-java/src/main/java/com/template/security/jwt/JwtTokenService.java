package com.template.security.jwt;

import com.template.security.config.SecurityProperties;
import com.template.security.auth.AppUserPrincipal;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;

/**
 * JWT token 生成服务。
 */
@Service
public class JwtTokenService {

    private final SecurityProperties securityProperties;

    public JwtTokenService(SecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
    }

    /**
     * 创建 accessToken。
     *
     * @param userId   用户 ID
     * @param userName 用户名
     * @param roles    角色编码
     * @return JWT 字符串
     */
    public String createAccessToken(Long userId, String userName, List<String> roles) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(securityProperties.getAccessTokenMinutes() * 60);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("userName", userName)
                .claim("roles", roles)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(getSecretKey())
                .compact();
    }

    /**
     * 解析 accessToken。
     *
     * @param token JWT 字符串
     * @return 当前登录用户身份
     */
    public AppUserPrincipal parseAccessToken(String token) {
        var claims = Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        Long userId = Long.valueOf(claims.getSubject());
        String userName = claims.get("userName", String.class);
        Object rolesClaim = claims.get("roles");
        List<String> roles = rolesClaim instanceof List<?> values
                ? values.stream().map(String::valueOf).toList()
                : List.of();
        return new AppUserPrincipal(userId, userName, roles);
    }

    private SecretKey getSecretKey() {
        byte[] keyBytes = securityProperties.getJwtSecret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
