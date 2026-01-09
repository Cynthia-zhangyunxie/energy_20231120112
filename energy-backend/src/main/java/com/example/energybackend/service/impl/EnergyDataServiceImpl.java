package com.example.energybackend.service.impl;

import com.example.energybackend.dto.EnergyDataDTO;
import com.example.energybackend.entity.Device;
import com.example.energybackend.entity.EnergyData;
import com.example.energybackend.exception.BusinessException;
import com.example.energybackend.repository.EnergyDataRepository;
import com.example.energybackend.service.DeviceService;
import com.example.energybackend.service.EnergyDataService;
import com.example.energybackend.util.BeanCopyUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 能耗数据业务逻辑实现（最终修复版）
 * 解决：1. findTop1返回值类型不匹配 2. BeanCopyUtil泛型推断 3. 空指针防护
 */
@Service
@Slf4j
public class EnergyDataServiceImpl implements EnergyDataService {

    @Autowired
    private EnergyDataRepository energyDataRepository;

    @Autowired
    private DeviceService deviceService;

    @Override
    public EnergyDataDTO getLatestEnergyData(Long deviceId) {
        log.info("查询设备最新能耗数据：deviceId={}", deviceId);
        // 1. 校验设备是否存在
        Device device = deviceService.getDeviceById(deviceId);

        // 2. 兼容处理：无论Repository返回List还是Optional，统一转为Optional<EnergyData>
        List<EnergyData> dataList = energyDataRepository.findTop1ByDeviceIdOrderByCollectTimeDesc(deviceId);
        Optional<EnergyData> optionalData = dataList.stream().findFirst();

        // 3. 处理Optional：显式指定BeanCopyUtil泛型，避免推断失败
        return optionalData.map(data -> {
            // 显式声明泛型类型，解决Lambda中类型推断问题
            EnergyDataDTO dto = BeanCopyUtil.<EnergyDataDTO>copyBean(data, EnergyDataDTO.class);
            dto.setDeviceName(device.getName());
            return dto;
        }).orElseThrow(() -> new BusinessException("该设备暂无能耗数据"));
    }

    @Override
    public List<EnergyDataDTO> getRecentEnergyData(Long deviceId, Integer n) {
        log.info("查询设备最近{}条能耗数据：deviceId={}", n, deviceId);
        // 1. 校验设备是否存在
        Device device = deviceService.getDeviceById(deviceId);

        // 用Pageable指定条数（PageRequest.of(0, n) 表示第0页、每页n条）
        Pageable pageable = PageRequest.of(0, n, Sort.by(Sort.Direction.DESC, "collectTime"));
        List<EnergyData> dataList = energyDataRepository.findByDeviceIdOrderByCollectTimeDesc(deviceId, pageable);

        // 3. 空列表防护 + Stream转换（显式泛型）
        if (dataList.isEmpty()) {
            log.warn("设备{}暂无最近{}条能耗数据", deviceId, n);
            return List.of(); // 返回空列表，避免NPE
        }

        // 4. Stream转换：显式指定BeanCopyUtil泛型
        return dataList.stream()
                .map(data -> {
                    EnergyDataDTO dto = BeanCopyUtil.<EnergyDataDTO>copyBean(data, EnergyDataDTO.class);
                    dto.setDeviceName(device.getName());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public Double getTodayEnergyByBuilding(Long buildingId) {
        log.info("查询建筑今日用电量：buildingId={}", buildingId);
        // 1. 查询建筑今日能耗数据列表
        List<Double> energyList = energyDataRepository.calculateTodayEnergyByBuilding(buildingId);

        // 2. 空列表防护：避免sum()时出现0.0以外的异常
        if (energyList.isEmpty()) {
            log.warn("建筑{}今日暂无能耗数据", buildingId);
            return 0.0;
        }

        // 3. 计算总用电量（Stream求和）
        return energyList.stream()
                .mapToDouble(Double::doubleValue)
                .sum();
    }
}