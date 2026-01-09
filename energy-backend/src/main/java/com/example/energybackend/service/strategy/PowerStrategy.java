package com.example.energybackend.service.strategy;


import com.example.energybackend.entity.Device;
import java.math.BigDecimal;

// Pattern: Strategy（策略模式）- 定义功率计算策略接口
public interface PowerStrategy {
    /**
     * 计算设备实时功率
     * @param device 设备信息（含额定功率、房间 类型）
     * @return 实时功率（W）
     */
    BigDecimal calculatePower(Device device);
}