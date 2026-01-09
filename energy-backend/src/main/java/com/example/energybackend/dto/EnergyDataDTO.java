package com.example.energybackend.dto;


import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class EnergyDataDTO {
    private Long id;
    private Long deviceId;
    private String deviceName; // 设备名称（用于前端展示）
    private BigDecimal voltage;
    private BigDecimal current;
    private BigDecimal realPower;
    private BigDecimal totalEnergy;
    private LocalDateTime collectTime;
}