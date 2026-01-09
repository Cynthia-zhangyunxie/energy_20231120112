package com.example.energybackend.service.strategy.impl;

// 宿舍功率策略（低功率，严控违规电器）


import com.example.energybackend.entity.Device;
import com.example.energybackend.service.strategy.PowerStrategy;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalTime;
import java.util.Random;

// Pattern: Strategy（策略模式）- 宿舍设备功率计算策略
@Component("dormitoryPowerStrategy")
public class DormitoryPowerStrategy implements PowerStrategy {
    private final Random random = new Random();

    @Override
    public BigDecimal calculatePower(Device device) {
        BigDecimal ratedPower = device.getRatedPower();
        LocalTime now = LocalTime.now();
        BigDecimal power;

        // 日间 模式（08:00-22:00）：0.2-0.9倍额定功率
        if (now.isAfter(LocalTime.of(8, 0)) && now.isBefore(LocalTime.of(22, 0))) {
            double ratio = 0.2 + random.nextDouble() * 0.7; // 0.2~0.9
            power = ratedPower.multiply(new BigDecimal(ratio)).setScale(2, RoundingMode.HALF_UP);
        }
        // 夜间 模式（22:00-08:00）：10-100W
        else {
            power = new BigDecimal(10 + random.nextInt(91)).setScale(2, RoundingMode.HALF_UP);
        }
        return power;
    }
}

