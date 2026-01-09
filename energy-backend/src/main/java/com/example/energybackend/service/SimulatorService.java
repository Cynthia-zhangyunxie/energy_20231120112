package com.example.energybackend.service;



/**
 * 能耗数据模拟器业务逻辑接口
 */
public interface SimulatorService {
    /**
     * 初始化设备累计电量（项目启动时调用）
     */
    void initTotalEnergy();

    /**
     * 模拟生成能耗数据（定时任务调用）
     */
    void simulateEnergyData();
}