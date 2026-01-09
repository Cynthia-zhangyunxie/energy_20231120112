package com.example.energybackend.controller;

import com.example.energybackend.dto.ResponseResult;
import com.example.energybackend.util.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/test/jwt")
public class TestJwtController {

    @Autowired
    private JWTUtil jwtUtil;

    @GetMapping("/validate")
    public ResponseResult<Map<String, Object>> validateToken(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Map<String, Object> result = new HashMap<>();

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            result.put("valid", false);
            result.put("error", "Authorization头格式不正确或缺失");
            return ResponseResult.success(result);
        }

        String token = authHeader.substring(7);
        result.put("tokenLength", token.length());
        result.put("tokenPreview", token.substring(0, Math.min(30, token.length())) + "...");

        try {
            // 验证Token
            String username = jwtUtil.getUsernameFromToken(token);
            String role = jwtUtil.getRoleFromToken(token);
            boolean expired = jwtUtil.isTokenExpired(token);

            result.put("valid", !expired);
            result.put("username", username);
            result.put("role", role);
            result.put("expired", expired);
            result.put("message", expired ? "Token已过期" : "Token有效");

        } catch (Exception e) {
            result.put("valid", false);
            result.put("error", e.getMessage());
            result.put("exceptionType", e.getClass().getName());
        }

        return ResponseResult.success(result);
    }

    @GetMapping("/simple")
    public ResponseResult<String> simpleTest() {
        return ResponseResult.success("JWT测试接口正常");
    }
}
