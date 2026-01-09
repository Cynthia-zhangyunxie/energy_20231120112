package com.example.energybackend.service.impl;



import com.example.energybackend.dto.LoginDTO;
import com.example.energybackend.dto.UserDTO;
import com.example.energybackend.entity.User;
import com.example.energybackend.exception.BusinessException;
import com.example.energybackend.repository.UserRepository;
import com.example.energybackend.service.UserService;
import com.example.energybackend.util.BeanCopyUtil;
import com.example.energybackend.util.JWTUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JWTUtil jwtUtil;

    @Override
    public String login(LoginDTO loginDTO) {
        log.info("用户登录：username={}", loginDTO.getUsername());
        // 1. 查询用户（状态正常）
        User user = userRepository.findByUsernameAndStatus(loginDTO.getUsername(), 1)
                .orElseThrow(() -> new BusinessException("用户名或密码错误"));

        // 2. 校验密码（直接明文对比）
        log.info("调试信息：输入密码='{}'，数据库密码='{}'", loginDTO.getPassword(), user.getPassword());
        if (!loginDTO.getPassword().equals(user.getPassword())) {
            log.error("密码不匹配：输入密码长度={}, 数据库密码长度={}", 
                     loginDTO.getPassword().length(), user.getPassword().length());
            throw new BusinessException("用户名或密码错误");
        }

        // 3. 生成JWT令牌（包含用户名和角色）
        String token = jwtUtil.generateToken(user.getUsername(), user.getRole());
        log.info("用户登录成功：username={}，token={}", user.getUsername(), token);
        return token;
    }

    @Override
    public User getUserByUsername(String username) {
        log.info("根据用户名查询用户：username={}", username);
        return userRepository.findByUsernameAndStatus(username, 1)
                .orElseThrow(() -> new BusinessException("用户不存在或已禁用"));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addUser(UserDTO userDTO) {
        log.info("新增用户：username={}", userDTO.getUsername());
        // 1. 校验用户名唯一性
        Optional<User> existingUser = userRepository.findByUsernameAndStatus(userDTO.getUsername(), 1);
        if (existingUser.isPresent()) {
            throw new BusinessException("用户名已存在");
        }

        // 2. 密码加密（MD5）
        User user = BeanCopyUtil.copyBean(userDTO, User.class);
        String md5Password = DigestUtils.md5DigestAsHex(userDTO.getPassword().getBytes());
        user.setPassword(md5Password);

        // 3. 保存用户
        userRepository.save(user);
        log.info("新增用户成功：username={}", user.getUsername());
    }

    @Override
    public List<User> getAllUsers() {
        log.info("查询所有用户");
        return userRepository.findAll();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(UserDTO userDTO) {
        log.info("修改用户信息：id={}", userDTO.getId());
        // 1. 查询用户是否存在
        User user = userRepository.findById(userDTO.getId())
                .orElseThrow(() -> new BusinessException("用户不存在"));

        // 2. 校验用户名唯一性（若修改用户名）
        if (!user.getUsername().equals(userDTO.getUsername())) {
            Optional<User> existingUser = userRepository.findByUsernameAndStatus(userDTO.getUsername(), 1);
            if (existingUser.isPresent()) {
                throw new BusinessException("用户名已存在");
            }
            user.setUsername(userDTO.getUsername());
        }

        // 3. 密码重置（若传入新密码）
        if (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()) {
            String md5Password = DigestUtils.md5DigestAsHex(userDTO.getPassword().getBytes());
            user.setPassword(md5Password);
        }

        // 4. 更新其他信息
        user.setRole(userDTO.getRole());
        user.setNickname(userDTO.getNickname());
        user.setStatus(userDTO.getStatus());

        // 5. 保存修改
        userRepository.save(user);
        log.info("修改用户信息成功：id={}", user.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long id) {
        log.info("删除用户：id={}", id);
        // 1. 查询用户是否存在
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        // 2. 物理删除（也可改为逻辑删除：设置status=0）
        userRepository.delete(user);
        log.info("删除用户成功：id={}", id);
    }
}