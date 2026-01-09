package com.example.energybackend.filter;

import com.example.energybackend.realm.JwtRealm;
import com.example.energybackend.util.JWTUtil;
import org.apache.shiro.web.filter.AccessControlFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class JwtFilter extends AccessControlFilter {
    private static final Logger logger = LoggerFactory.getLogger(JwtFilter.class);

    private final JWTUtil jwtUtil;

    @Autowired
    public JwtFilter(JWTUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
        logger.info("JwtFilter被初始化，jwtUtil是否为空: " + (jwtUtil == null ? "是" : "否"));
    }

    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) throws Exception {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String requestURI = httpRequest.getRequestURI();
        String servletPath = httpRequest.getServletPath();

        logger.debug("=== JwtFilter开始处理请求 ===");
        logger.debug("请求URI: " + requestURI);
        logger.debug("Servlet路径: " + servletPath);
        logger.debug("请求方法: " + httpRequest.getMethod());

        // 检查jwtUtil是否为空
        if (jwtUtil == null) {
            logger.error("jwtUtil为null，无法验证Token");
            return false;
        }

        // 检查是否是登录请求 - 如果是，直接放行（即使Shiro配置有问题）
        if (servletPath.equals("/auth/login") || requestURI.endsWith("/auth/login")) {
            logger.debug("检测到登录请求，直接放行");
            return true;
        }

        String authHeader = httpRequest.getHeader("Authorization");

        if (authHeader != null) {
            logger.debug("Authorization头存在，长度: " + authHeader.length());
            logger.debug("Authorization头前50字符: " + authHeader.substring(0, Math.min(50, authHeader.length())) + "...");
        } else {
            logger.debug("Authorization头不存在");
            return false;
        }

        // 1. Token为空，不允许访问
        if (!StringUtils.hasText(authHeader)) {
            logger.warn("Authorization头为空或未提供");
            return false;
        }

        // 2. 移除Token前缀（Bearer ）
        String token = authHeader;
        if (authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            logger.debug("移除Bearer前缀后Token长度: " + token.length());
        } else {
            logger.warn("Authorization头没有Bearer前缀");
            return false;
        }

        // 3. 校验Token有效性
        try {
            logger.debug("开始验证Token...");

            // 从Token中获取用户名
            String username = jwtUtil.getUsernameFromToken(token);
            logger.debug("Token中的用户名: " + username);

            // 验证Token是否过期
            boolean expired = jwtUtil.isTokenExpired(token);
            if (expired) {
                logger.warn("Token已过期");
                return false;
            }

            // 获取角色
            String role = jwtUtil.getRoleFromToken(token);
            logger.debug("Token中的角色: " + role);

            // Token有效，创建Shiro Token并绑定到Subject
            JwtRealm.JwtToken jwtToken = new JwtRealm.JwtToken(token);
            getSubject(request, response).login(jwtToken);

            logger.debug("✅ Token验证成功，允许访问");
            return true;

        } catch (Exception e) {
            // Token无效或过期
            logger.error("❌ Token验证失败: " + e.getMessage());
            return false;
        }
    }

    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        httpResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
        httpResponse.setContentType("application/json;charset=UTF-8");
        httpResponse.getWriter().write("{\"code\":401,\"msg\":\"未授权或Token失效，请重新登录\"}");

        logger.warn("返回401未授权响应");
        return false;
    }
}
