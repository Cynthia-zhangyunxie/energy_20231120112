package com.example.energybackend.service.impl;

import com.example.energybackend.entity.Device;
import com.example.energybackend.entity.EnergyData;
import com.example.energybackend.event.EnergyAnomalyEvent;
import com.example.energybackend.repository.EnergyDataRepository;
import com.example.energybackend.service.DeviceService;
import com.example.energybackend.service.SimulatorService;
import com.example.energybackend.service.strategy.PowerStrategyFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
public class SimulatorServiceImpl implements SimulatorService {

    @Autowired
    private DeviceService deviceService;

    @Autowired
    private EnergyDataRepository energyDataRepository;

    @Autowired
    private PowerStrategyFactory powerStrategyFactory;

    @Autowired
    private ApplicationContext applicationContext; // 用于发布事件

    @Value("${simulator.anomaly-rate}")
    private Integer anomalyRate; // 每N条正常数据生成1条异常数据

    private final Random random = new Random();
    private final ConcurrentHashMap<Long, AtomicInteger> deviceDataCount = new ConcurrentHashMap<>(); // 设备数据计数
    private final ConcurrentHashMap<Long, BigDecimal> deviceTotalEnergy = new ConcurrentHashMap<>(); // 设备累计用电量缓存

    // 项目启动时初始化设备累计电量
    @PostConstruct
    @Override
    public void initTotalEnergy() {
        log.info("初始化设备累计电量缓存");
        List<Device> devices = deviceService.getAllDevices();
        for (Device device : devices) {
            // 查询设备最新一条能耗数据的累计电量
            List<EnergyData> dataList = energyDataRepository.findTop1ByDeviceIdOrderByCollectTimeDesc(device.getId());
            // 将List转为Optional，兼容ifPresent方法
            Optional<EnergyData> latestData = dataList.stream().findFirst();
            latestData.ifPresent(data -> {
                // 确保EnergyData类存在getTotalEnergy()方法
                deviceTotalEnergy.put(device.getId(), data.getTotalEnergy());
            });
            // 初始化数据计数器
            deviceDataCount.put(device.getId(), new AtomicInteger(0));
        }
    }

    // 定时任务：每5秒生成一次能耗数据
    @Scheduled(cron = "0/5 * * * * ?")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void simulateEnergyData() {
        log.info("开始生成能耗数据...");
        List<Device> devices = deviceService.getAllDevices();

        for (Device device : devices) {
            if (!"ONLINE".equals(device.getStatus())) {
                continue; // 离线/停用设备不生成数据
            }

            AtomicInteger count = deviceDataCount.get(device.getId());
            int currentCount = count.incrementAndGet();

            // 生成能耗数据（正常/异常）
            EnergyData energyData = generateData(device, currentCount % anomalyRate == 0);

            // 保存能耗数据
            energyDataRepository.save(energyData);

            // 重置计数器
            if (currentCount >= anomalyRate) {
                count.set(0);
            }
        }
        log.info("能耗数据生成完成");
    }

    // 生成单台设备的能耗数据（支持正常/异常数据）
    private EnergyData generateData(Device device, boolean isAnomaly) {
        EnergyData data = new EnergyData();
        data.setDeviceId(device.getId());
        data.setCollectTime(LocalDateTime.now());

        BigDecimal voltage;
        BigDecimal realPower;
        String alarmType = null;
        String alarmDetail = null;

        // 生成异常数据
        if (isAnomaly) {
            int anomalyType = random.nextInt(2); // 0=过载，1=电压异常
            if (anomalyType == 0) {
                // 异常类型A：过载（1.2倍额定功率）
                realPower = device.getRatedPower().multiply(new BigDecimal(1.2)).setScale(2, RoundingMode.HALF_UP);
                // 电压正常
                voltage = generateNormalVoltage();
                // 记录告警信息
                alarmType = "OVERLOAD";
                alarmDetail = "功率过载：实时功率" + realPower + "W，额定功率" + device.getRatedPower() + "W";
            } else {
                // 异常类型B：电压异常（180V或260V）
                voltage = random.nextBoolean() ? new BigDecimal("180.00") : new BigDecimal("260.00");
                // 功率正常（用策略模式计算）
                realPower = powerStrategyFactory.getStrategy(device.getRoomNo()).calculatePower(device);
                // 记录告警信息
                alarmType = "VOLTAGE_ABNORMAL";
                alarmDetail = "电压异常：" + voltage + "V，偏离标准电压（220V）±10%以上";
            }
        }
        // 生成正常数据
        else {
            // 1. 生成正常电压（210-235V正态分布）
            voltage = generateNormalVoltage();
            // 2. 生成正常功率（策略模式选择对应策略）
            realPower = powerStrategyFactory.getStrategy(device.getRoomNo()).calculatePower(device);
        }

        // 3. 计算电流（I=P/U）
        BigDecimal current = realPower.divide(voltage, 2, RoundingMode.HALF_UP);

        // 4. 计算累计用电量（每5秒生成一次，累计=上一次累计 + 实时功率(W) * 5秒 / 3600秒 / 1000 = kWh）
        BigDecimal lastTotal = deviceTotalEnergy.getOrDefault(device.getId(), BigDecimal.ZERO);
        BigDecimal addEnergy = realPower.multiply(new BigDecimal("5"))
                .divide(new BigDecimal("3600000"), 2, RoundingMode.HALF_UP);
        BigDecimal totalEnergy = lastTotal.add(addEnergy);

        // 5. 赋值并更新缓存
        data.setVoltage(voltage);
        data.setCurrent(current);
        data.setRealPower(realPower);
        data.setTotalEnergy(totalEnergy);
        deviceTotalEnergy.put(device.getId(), totalEnergy);

        // 6. 数据完全赋值后，再发布异常告警事件
        if (isAnomaly) {
            publishAnomalyEvent(device, data, alarmType, alarmDetail);
        }

        return data;
    }

    // 生成正常电压（210-235V正态分布）
    private BigDecimal generateNormalVoltage() {
        // 正态分布：均值220，标准差5
        double voltage = random.nextGaussian() * 5 + 220;
        // 限制在210-235V之间
        voltage = Math.max(210, Math.min(235, voltage));
        return new BigDecimal(voltage).setScale(2, RoundingMode.HALF_UP);
    }

    // 发布异常事件
    private void publishAnomalyEvent(Device device, EnergyData data, String alarmType, String detail) {
        applicationContext.publishEvent(new EnergyAnomalyEvent(this, device, data, alarmType, detail));
    }
}