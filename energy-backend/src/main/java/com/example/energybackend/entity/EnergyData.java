package com.example.energybackend.entity;


import lombok.Data;
import org.hibernate.annotations.DynamicInsert;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "t_energy_data")
@DynamicInsert
public class EnergyData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 主键ID

    @Column(name = "device_id", nullable = false)
    private Long deviceId; // 外键_所属设备ID

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal voltage; // 电压（V）

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal current; // 电流（A）

    @Column(name = "real_power", nullable = false, precision = 10, scale = 2)
    private BigDecimal realPower; // 实时功率（W）

    @Column(name = "total_energy", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalEnergy; // 累计用电量（kWh）

    @Column(name = "collect_time", nullable = false)
    private LocalDateTime collectTime; // 采集时间戳

    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime; // 创建时间

    @PrePersist
    public void prePersist() {
        this.createTime = LocalDateTime.now();
    }
}