package com.example.energybackend.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.logging.Logger;

@Component
public class JWTUtil {
    private static final Logger logger = Logger.getLogger(JWTUtil.class.getName());

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration:3600000}")  // 默认3600000毫秒=1小时
    private Long expiration;

    // 生成JWT令牌（包含用户名和角色）
    public String generateToken(String username, String role) {
        logger.info("=== 生成Token开始 ===");
        logger.info("用户名: " + username);
        logger.info("角色: " + role);
        logger.info("配置的过期时间: " + expiration + " 毫秒");

        Date now = new Date();
        logger.info("当前时间(毫秒): " + now.getTime());
        logger.info("当前时间(秒): " + now.getTime() / 1000);

        // 这里的关键：配置中的expiration是毫秒，直接加上去
        Date expiryDate = new Date(now.getTime() + expiration);
        logger.info("过期时间(毫秒): " + expiryDate.getTime());
        logger.info("过期时间(秒): " + expiryDate.getTime() / 1000);

        String token = Jwts.builder()
                .setSubject(username)
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();

        logger.info("生成的Token长度: " + token.length());
        logger.info("Token前50字符: " + token.substring(0, Math.min(50, token.length())) + "...");
        logger.info("=== 生成Token结束 ===\n");

        return token;
    }

    // 从令牌中获取用户名
    public String getUsernameFromToken(String token) {
        try {
            String username = getClaimsFromToken(token).getSubject();
            logger.info("从Token获取用户名: " + username);
            return username;
        } catch (Exception e) {
            logger.severe("获取用户名失败: " + e.getMessage());
            throw e;
        }
    }

    // 从令牌中获取角色
    public String getRoleFromToken(String token) {
        try {
            String role = (String) getClaimsFromToken(token).get("role");
            logger.info("从Token获取角色: " + role);
            return role;
        } catch (Exception e) {
            logger.severe("获取角色失败: " + e.getMessage());
            throw e;
        }
    }

    // 验证令牌是否过期
    public boolean isTokenExpired(String token) {
        try {
            Date expiration = getClaimsFromToken(token).getExpiration();
            Date now = new Date();
            boolean expired = expiration.before(now);

            logger.info("=== Token过期检查 ===");
            logger.info("Token过期时间: " + expiration + " (" + expiration.getTime() + "毫秒)");
            logger.info("当前时间: " + now + " (" + now.getTime() + "毫秒)");
            logger.info("是否过期: " + expired);

            // 额外信息：显示时间差
            long diff = expiration.getTime() - now.getTime();
            logger.info("距离过期还有: " + diff + "毫秒 (" + (diff / 1000) + "秒)");

            return expired;
        } catch (Exception e) {
            logger.severe("检查Token过期失败: " + e.getMessage());
            throw e;
        }
    }

    // 解析令牌获取Claims
    private Claims getClaimsFromToken(String token) {
        try {
            logger.info("=== 解析Token开始 ===");
            logger.info("Token长度: " + token.length());
            logger.info("Token: " + token.substring(0, Math.min(50, token.length())) + "...");

            Claims claims = Jwts.parser()
                    .setSigningKey(secret)
                    .parseClaimsJws(token)
                    .getBody();

            logger.info("Token解析成功");
            logger.info("Subject: " + claims.getSubject());
            logger.info("Role: " + claims.get("role"));
            logger.info("签发时间(IssuedAt): " + claims.getIssuedAt());
            logger.info("过期时间(Expiration): " + claims.getExpiration());
            logger.info("=== 解析Token结束 ===\n");

            return claims;
        } catch (Exception e) {
            logger.severe("解析Token失败: " + e.getMessage());
            logger.severe("异常类型: " + e.getClass().getName());
            e.printStackTrace();
            throw e;
        }
    }

    // 新增方法：验证Token有效性
    public boolean validateToken(String token) {
        try {
            // 先检查Token是否可以解析
            Claims claims = getClaimsFromToken(token);

            // 检查是否过期
            boolean expired = isTokenExpired(token);

            // 检查主题是否为空
            String subject = claims.getSubject();
            boolean subjectValid = subject != null && !subject.trim().isEmpty();

            logger.info("Token验证结果:");
            logger.info("  是否可以解析: 是");
            logger.info("  是否过期: " + (expired ? "是" : "否"));
            logger.info("  主题是否有效: " + (subjectValid ? "是" : "否"));

            return !expired && subjectValid;
        } catch (Exception e) {
            logger.severe("Token验证失败: " + e.getMessage());
            return false;
        }
    }
}
