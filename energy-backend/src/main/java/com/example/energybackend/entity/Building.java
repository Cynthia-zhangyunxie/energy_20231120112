package com.example.energybackend.entity;

import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "t_building")
@DynamicInsert // 动态插入（只插入非空字段）
@DynamicUpdate // 动态更新（只更新修改的字段）
public class Building {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 主键ID

    @Column(nullable = false, length = 50)
    private String name; // 建筑名称

    @Column(name = "location_code", nullable = false, length = 20)
    private String locationCode; // 位置编号

    @Column(name = "floor_count", nullable = false)
    private Integer floorCount; // 楼层数

    @Column(name = "use_type", nullable = false, length = 30)
    private String useType; // 建筑用途分类

    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime; // 创建时间

    @Column(name = "update_time", nullable = false)
    private LocalDateTime updateTime; // 更新时间

    @Column(name = "is_deleted", nullable = false)
    private Integer isDeleted = 0; // 是否删除（0-未删，1-已删）

    // 自动填充创建时间和更新时间
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