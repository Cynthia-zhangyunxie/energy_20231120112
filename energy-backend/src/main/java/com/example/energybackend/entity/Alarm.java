package com.example.energybackend.entity;


import lombok.Data;
import org.hibernate.annotations.DynamicInsert;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "t_alarm")
@DynamicInsert
public class Alarm {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 主键ID

    @Column(name = "device_id", nullable = false)
    private Long deviceId; // 外键_所属设备ID

    @Column(name = "alarm_type", nullable = false, length = 30)
    private String alarmType; // 告警类型（OVERLOAD/VOLTAGE_ABNORMAL）

    @Column(name = "alarm_value", nullable = false, precision = 10, scale = 2)
    private BigDecimal alarmValue; // 告警数值

    @Column(name = "alarm_detail", nullable = false, length = 200)
    private String alarmDetail; // 告警详情

    @Column(name = "trigger_time", nullable = false)
    private LocalDateTime triggerTime; // 触发时间

    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime; // 创建时间

    @PrePersist
    public void prePersist() {
        this.createTime = LocalDateTime.now();
    }
}