package com.example.energybackend.event;

import com.example.energybackend.entity.Alarm;
import com.example.energybackend.entity.Device;
import com.example.energybackend.entity.EnergyData;

import com.example.energybackend.repository.AlarmRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

// Pattern: Observer（观察者模式）- 告警监听器（观察者），异步处理告警
@Component
@Slf4j
public class AlarmListener {
    @Autowired
    private AlarmRepository alarmRepository;

    // 监听异常事件，异步处理（避免阻塞数据采集）
    @Async
    @EventListener(EnergyAnomalyEvent.class)
    @Transactional(rollbackFor = Exception.class)
    public void handleAnomalyEvent(EnergyAnomalyEvent event) {
        Device device = event.getDevice();
        EnergyData energyData = event.getEnergyData();
        String alarmType = event.getAlarmType();
        String alarmDetail = event.getAlarmDetail();

        // 构建告警实体
        Alarm alarm = new Alarm();
        alarm.setDeviceId(device.getId());
        alarm.setAlarmType(alarmType);
        alarm.setAlarmValue("OVERLOAD".equals(alarmType) ? energyData.getRealPower() : energyData.getVoltage());
        alarm.setAlarmDetail(alarmDetail);
        alarm.setTriggerTime(energyData.getCollectTime());

        // 保存告警记录
        alarmRepository.save(alarm);
        log.info("设备[{}]触发告警：{}，详情：{}", device.getSn(), alarmType, alarmDetail);
    }
}