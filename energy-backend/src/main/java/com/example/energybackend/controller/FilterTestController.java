package com.example.energybackend.controller;


import com.example.energybackend.dto.ResponseResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/filter-test")
public class FilterTestController {

    private static final Logger logger = LoggerFactory.getLogger(FilterTestController.class);

    @GetMapping("/headers")
    public ResponseResult<Map<String, Object>> testHeaders(HttpServletRequest request,
                                                           @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Map<String, Object> result = new HashMap<>();

        // 记录所有请求头
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            headers.put(headerName, request.getHeader(headerName));
        }
        result.put("headers", headers);

        // 记录请求信息
        result.put("requestURI", request.getRequestURI());
        result.put("servletPath", request.getServletPath());
        result.put("method", request.getMethod());
        result.put("authHeader", authHeader);

        logger.info("FilterTestController被调用，请求URI: {}", request.getRequestURI());
        logger.info("Authorization头: {}", authHeader);

        return ResponseResult.success(result);
    }

    @GetMapping("/simple")
    public ResponseResult<String> simple() {
        logger.info("FilterTestController.simple被调用");
        return ResponseResult.success("简单测试接口");
    }
}
