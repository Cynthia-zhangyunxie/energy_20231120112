package com.example.energybackend.service.strategy;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// Pattern: Factory（工厂模式）- 功率策略工厂，用于获取对应策略
@Component
public class PowerStrategyFactory {
    // 存储策略Bean：key=策略名称，value=策略实例
    private final Map<String, PowerStrategy> strategyMap = new ConcurrentHashMap<>();

    // 自动注入所有PowerStrategy实现类，存入Map
    @Autowired
    public PowerStrategyFactory(Map<String, PowerStrategy> strategyMap) {
        this.strategyMap.putAll(strategyMap);
    }

    /**
     * 根据设备房间类型获取对应策略
     * @param roomNo 房间 号（如301=宿舍，101=教室，LAB1=实验室）
     * @return 功率计算策略
     */
    public PowerStrategy getStrategy(String roomNo) {
        if (roomNo.contains("LAB") || roomNo.contains("实验室")) {
            return strategyMap.get("labPowerStrategy");
        } else if (roomNo.matches("\\d{3}") || roomNo.contains("宿舍")) { // 3位数字房间号=宿舍
            return strategyMap.get("dormitoryPowerStrategy");
        } else { // 其他=教室/公共区域
            return strategyMap.get("classroomPowerStrategy");
        }
    }
}