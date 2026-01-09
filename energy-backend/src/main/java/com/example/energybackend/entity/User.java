package com.example.energybackend.entity;


import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "t_user")
@DynamicInsert
@DynamicUpdate
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 主键ID

    @Column(nullable = false, length = 50, unique = true)
    private String username; // 用户名

    @Column(nullable = false, length = 100)
    private String password; // 加密后的密码

    @Column(nullable = false, length = 20)
    private String role; // 角色（ADMIN/NORMAL）

    private String nickname; // 昵称

    @Column(nullable = false)
    private Integer status = 1; // 状态（1-正常，0-禁用）

    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime; // 创建时间

    @Column(name = "update_time", nullable = false)
    private LocalDateTime updateTime; // 更新时间

    @PrePersist
    public void prePersist() {
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updateTime = LocalDateTime.now();
    }
}