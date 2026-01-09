package com.example.energybackend.service.impl;



import com.example.energybackend.dto.DeviceDTO;
import com.example.energybackend.entity.Device;
import com.example.energybackend.exception.BusinessException;
import com.example.energybackend.repository.DeviceRepository;
import com.example.energybackend.service.DeviceService;
import com.example.energybackend.util.BeanCopyUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class DeviceServiceImpl implements DeviceService {

    @Autowired
    private DeviceRepository deviceRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addDevice(DeviceDTO deviceDTO) {
        // 1. 校验SN唯一性
        Optional<Device> existingSn = deviceRepository.findBySnAndIsDeleted(deviceDTO.getSn(), 0);
        if (existingSn.isPresent()) {
            throw new BusinessException("设备序列号SN已存在，请勿重复添加");
        }

        // 2. 校验房间 唯一性（同一房间只能有一个有效设备）
        Optional<Device> existingRoom = deviceRepository.findByRoomNoAndIsDeleted(deviceDTO.getRoomNo(), 0);
        if (existingRoom.isPresent()) {
            throw new BusinessException("房间[" + deviceDTO.getRoomNo() + "]已绑定设备[" + existingRoom.get().getSn() + "]，请勿重复绑定");
        }

        // 3. DTO转Entity并保存
        Device device = BeanCopyUtil.copyBean(deviceDTO, Device.class);
        deviceRepository.save(device);
        log.info("新增设备成功：SN={}", device.getSn());
    }

    @Override
    public List<Device> getAllDevices() {
        log.info("查询所有未删除的设备");
        return deviceRepository.findByIsDeletedOrderByBuildingIdAsc(0);
    }

    @Override
    public Device getDeviceById(Long id) {
        log.info("根据ID查询设备：id={}", id);
        return deviceRepository.findById(id)
                .orElseThrow(() -> new BusinessException("设备不存在或已删除"));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void replaceDevice(Long oldDeviceId, DeviceDTO newDeviceDTO) {
        // 1. 查询旧设备
        Device oldDevice = getDeviceById(oldDeviceId);
        if ("STOPPED".equals(oldDevice.getStatus())) {
            throw new BusinessException("旧设备已停用，无需更换");
        }

        // 2. 校验新设备SN唯一性
        Optional<Device> existingSn = deviceRepository.findBySnAndIsDeleted(newDeviceDTO.getSn(), 0);
        if (existingSn.isPresent()) {
            throw new BusinessException("新设备序列号SN已存在");
        }

        // 3. 旧设备置为STOPPED
        oldDevice.setStatus("STOPPED");
        deviceRepository.save(oldDevice);

        // 4. 新增新设备（绑定旧设备的房间）
        Device newDevice = BeanCopyUtil.copyBean(newDeviceDTO, Device.class);
        newDevice.setRoomNo(oldDevice.getRoomNo()); // 绑定旧设备的房间
        newDevice.setBuildingId(oldDevice.getBuildingId()); // 绑定旧设备的建筑
        deviceRepository.save(newDevice);

        log.info("更换设备成功：旧设备ID={}，新设备SN={}", oldDeviceId, newDevice.getSn());
    }

    @Override
    public List<Device> getDevicesByBuildingId(Long buildingId) {
        log.info("根据建筑ID查询设备：buildingId={}", buildingId);
        return deviceRepository.findByBuildingIdAndIsDeleted(buildingId, 0);
    }
}