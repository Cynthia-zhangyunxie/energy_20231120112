package com.example.energybackend.entity;


import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "t_device",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_device_sn", columnNames = "sn"),
                @UniqueConstraint(name = "uk_device_room", columnNames = {"room_no", "is_deleted"})
        })
@DynamicInsert
@DynamicUpdate
public class Device {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 主键ID

    @Column(nullable = false, length = 50)
    private String name; // 设备名称

    @Column(nullable = false, length = 50)
    private String sn; // 唯一设备序列号

    @Column(nullable = false, length = 20)
    private String status; // 通讯状态（ONLINE/OFFLINE/STOPPED）

    @Column(name = "rated_power", nullable = false, precision = 10, scale = 2)
    private BigDecimal ratedPower; // 额定功率阈值（W）

    @Column(name = "building_id", nullable = false)
    private Long buildingId; // 外键_所属建筑ID

    @Column(name = "room_no", nullable = false, length = 20)
    private String roomNo; // 所属房间号

    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime; // 创建时间

    @Column(name = "update_time", nullable = false)
    private LocalDateTime updateTime; // 更新时间

    @Column(name = "is_deleted", nullable = false)
    private Integer isDeleted = 0; // 是否删除

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