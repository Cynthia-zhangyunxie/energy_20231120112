package com.example.energybackend.repository;


import com.example.energybackend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // 根据用户名查询用户（登录用）
    Optional<User> findByUsernameAndStatus(String username, Integer status);
}