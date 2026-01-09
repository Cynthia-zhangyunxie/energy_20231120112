package com.example.energybackend.controller;

import com.example.energybackend.dto.ResponseResult;
import com.example.energybackend.dto.UserDTO;
import com.example.energybackend.entity.User;
import com.example.energybackend.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户管理控制器（仅管理员可操作）
 */
@RestController
@RequestMapping("/user")
@Api(tags = "用户管理接口")
@RequiresRoles("ADMIN") // 所有接口仅管理员可访问
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/add")
    @ApiOperation("新增用户")
    public ResponseResult<Void> addUser(@Validated(UserDTO.AddGroup.class) @RequestBody UserDTO userDTO) {
        userService.addUser(userDTO);
        return ResponseResult.success();
    }

    @GetMapping("/list")
    @ApiOperation("查询所有用户")
    public ResponseResult<List<User>> getAllUsers() {
        return ResponseResult.success(userService.getAllUsers());
    }

    @PutMapping("/update")
    @ApiOperation("修改用户信息")
    public ResponseResult<Void> updateUser(@Validated(UserDTO.UpdateGroup.class) @RequestBody UserDTO userDTO) {
        userService.updateUser(userDTO);
        return ResponseResult.success();
    }

    @DeleteMapping("/delete/{id}")
    @ApiOperation("删除用户")
    public ResponseResult<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseResult.success();
    }

    @GetMapping("/username/{username}")
    @ApiOperation("根据用户名查询用户")
    public ResponseResult<User> getUserByUsername(@PathVariable String username) {
        return ResponseResult.success(userService.getUserByUsername(username));
    }
}