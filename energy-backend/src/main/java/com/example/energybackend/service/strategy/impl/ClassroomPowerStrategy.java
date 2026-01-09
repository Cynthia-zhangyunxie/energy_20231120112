package com.example.energybackend.service.strategy.impl;

import com.example.energybackend.entity.Device;
import com.example.energybackend.service.strategy.PowerStrategy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalTime;
import java.util.Random;

// 教室功率策略（中高功率，含空调）
@Component("classroomPowerStrategy")
public class ClassroomPowerStrategy implements PowerStrategy {
    private final Random random = new Random();

    @Override
    public BigDecimal calculatePower(Device device) {
        BigDecimal ratedPower = device.getRatedPower();
        LocalTime now = LocalTime.now();
        BigDecimal power;

        // 日间 模式（08:00-22:00）：0.3-0.8倍额定功率（教室使用频率高）
        if (now.isAfter(LocalTime.of(8, 0)) && now.isBefore(LocalTime.of(22, 0))) {
            double ratio = 0.3 + random.nextDouble() * 0.5; // 0.3~0.8
            power = ratedPower.multiply(new BigDecimal(ratio)).setScale(2, RoundingMode.HALF_UP);
        }
        // 夜间 模式（22:00-08:00）：50-200W（空调待机）
        else {
            power = new BigDecimal(50 + random.nextInt(151)).setScale(2, RoundingMode.HALF_UP);
        }
        return power;
    }
}

