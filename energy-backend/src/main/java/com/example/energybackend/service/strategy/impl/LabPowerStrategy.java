package com.example.energybackend.service.strategy.impl;

import com.example.energybackend.entity.Device;
import com.example.energybackend.service.strategy.PowerStrategy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalTime;
import java.util.Random;
@Component("labPowerStrategy")
public class LabPowerStrategy implements PowerStrategy {
    private final Random random = new Random();

    @Override
    public BigDecimal calculatePower(Device device) {
        BigDecimal ratedPower = device.getRatedPower();
        LocalTime now = LocalTime.now();
        BigDecimal power;

        // 日间 模式（08:00-22:00）：0.5-0.95倍额定功率（实验室设备高负载）
        if (now.isAfter(LocalTime.of(8, 0)) && now.isBefore(LocalTime.of(22, 0))) {
            double ratio = 0.5 + random.nextDouble() * 0.45; // 0.5~0.95
            power = ratedPower.multiply(new BigDecimal(ratio)).setScale(2, RoundingMode.HALF_UP);
        }
        // 夜间 模式（22:00-08:00）：300-1000W（部分设备待机运行）
        else {
            power = new BigDecimal(300 + random.nextInt(701)).setScale(2, RoundingMode.HALF_UP);
        }
        return power;
    }
}