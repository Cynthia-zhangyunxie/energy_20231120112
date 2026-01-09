package com.example.energybackend.service;



import com.example.energybackend.dto.DeviceDTO;
import com.example.energybackend.entity.Device;

import java.util.List;

/**
 * 设备管理业务逻辑接口
 */
public interface DeviceService {
    /**
     * 新增设备（校验SN唯一性、房间 唯一性）
     * @param deviceDTO 设备DTO
     */
    void addDevice(DeviceDTO deviceDTO);

    /**
     * 查询所有未删除的设备
     * @return 设备列表
     */
    List<Device> getAllDevices();

    /**
     * 根据ID查询设备
     * @param id 设备ID
     * @return 设备实体
     */
    Device getDeviceById(Long id);

    /**
     * 更换设备（旧设备置为STOPPED，新设备绑定房间）
     * @param oldDeviceId 旧设备ID
     * @param newDeviceDTO 新设备DTO
     */
    void replaceDevice(Long oldDeviceId, DeviceDTO newDeviceDTO);

    /**
     * 根据建筑ID查询设备
     * @param buildingId 建筑ID
     * @return 该建筑下的设备列表
     */
    List<Device> getDevicesByBuildingId(Long buildingId);
}