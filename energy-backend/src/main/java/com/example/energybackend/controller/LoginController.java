package com.example.energybackend.controller;

import com.example.energybackend.dto.LoginDTO;
import com.example.energybackend.dto.ResponseResult;
import com.example.energybackend.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证授权控制器（登录/退出）
 */
@RestController
@RequestMapping("/auth")
@Api(tags = "认证授权接口")
public class LoginController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    @ApiOperation("用户登录（返回JWT令牌）")
    public ResponseResult<String> login(@Validated @RequestBody LoginDTO loginDTO) {
        String token = userService.login(loginDTO);
        return ResponseResult.success(token);
    }

    @PostMapping("/logout")
    @ApiOperation("用户退出登录")
    @RequiresAuthentication // 需要登录才能访问
    public ResponseResult<Void> logout() {
        SecurityUtils.getSubject().logout();
        return ResponseResult.success();
    }
}