package com.example.energybackend.event;


import com.example.energybackend.entity.EnergyData;
import com.example.energybackend.entity.Device;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

// Pattern: Observer（观察者模式）- 异常事件（被观察对象的状态变化）
@Getter
public class EnergyAnomalyEvent extends ApplicationEvent {
    private final Device device; // 异常设备
    private final EnergyData energyData; // 异常能耗数据
    private final String alarmType; // 告警类型
    private final String alarmDetail; // 告警详情

    public EnergyAnomalyEvent(Object source, Device device, EnergyData energyData, String alarmType, String alarmDetail) {
        super(source);
        this.device = device;
        this.energyData = energyData;
        this.alarmType = alarmType;
        this.alarmDetail = alarmDetail;
    }
}
