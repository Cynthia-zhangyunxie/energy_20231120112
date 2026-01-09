package com.example.energybackend.service;


import com.example.energybackend.dto.EnergyDataDTO;

import java.util.List;

/**
 * 能耗数据业务逻辑接口
 */
public interface EnergyDataService {
    /**
     * 查询设备最新能耗数据
     * @param deviceId 设备ID
     * @return 能耗数据DTO
     */
    EnergyDataDTO getLatestEnergyData(Long deviceId);

    /**
     * 查询设备最近N条能耗数据（用于折线图）
     * @param deviceId 设备ID
     * @param n 数据条数
     * @return 能耗数据DTO列表
     */
    List<EnergyDataDTO> getRecentEnergyData(Long deviceId, Integer n);

    /**
     * 查询建筑今日用电量（用于饼图）
     * @param buildingId 建筑ID
     * @return 今日用电量（kWh）
     */
    Double getTodayEnergyByBuilding(Long buildingId);
}