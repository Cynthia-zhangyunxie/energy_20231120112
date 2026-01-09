package com.example.energybackend.service;




import com.example.energybackend.dto.LoginDTO;
import com.example.energybackend.dto.UserDTO;
import com.example.energybackend.entity.User;

import java.util.List;

/**
 * 用户管理业务逻辑接口
 */
public interface UserService {
    /**
     * 用户登录（校验用户名、密码，生成JWT令牌）
     * @param loginDTO 登录DTO
     * @return JWT令牌
     */
    String login(LoginDTO loginDTO);

    /**
     * 根据用户名查询用户信息（用于Shiro权限校验）
     * @param username 用户名
     * @return 用户实体
     */
    User getUserByUsername(String username);

    /**
     * 新增用户（密码加密存储）
     * @param userDTO 用户DTO
     */
    void addUser(UserDTO userDTO);

    /**
     * 查询所有用户
     * @return 用户列表
     */
    List<User> getAllUsers();

    /**
     * 修改用户信息（支持密码重置）
     * @param userDTO 用户DTO
     */
    void updateUser(UserDTO userDTO);

    /**
     * 删除用户（逻辑删除/物理删除，此处为物理删除）
     * @param id 用户ID
     */
    void deleteUser(Long id);
}