package com.example.energybackend.service.impl;



import com.example.energybackend.entity.Alarm;
import com.example.energybackend.exception.BusinessException;
import com.example.energybackend.repository.AlarmRepository;
import com.example.energybackend.repository.DeviceRepository;
import com.example.energybackend.service.AlarmService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 告警记录业务逻辑实现
 */
@Service
@Slf4j
public class AlarmServiceImpl implements AlarmService {

    @Autowired
    private AlarmRepository alarmRepository;

    @Autowired
    private DeviceRepository deviceRepository;

    /**
     * 查询所有告警记录（按触发时间倒序）
     */
    @Override
    public List<Alarm> getAllAlarms() {
        log.info("查询所有告警记录");
        return alarmRepository.findAllByOrderByTriggerTimeDesc();
    }

    /**
     * 根据设备SN模糊查询告警记录
     */
    @Override
    public List<Alarm> searchAlarmsBySn(String sn) {
        if (sn == null || sn.trim().isEmpty()) {
            throw new BusinessException("设备SN不能为空");
        }
        log.info("根据设备SN模糊查询告警记录：sn={}", sn);
        return alarmRepository.findByDeviceSnLike(sn.trim());
    }

    /**
     * 根据设备ID查询告警记录
     */
    @Override
    public List<Alarm> getAlarmsByDeviceId(Long deviceId) {
        if (deviceId == null) {
            throw new BusinessException("设备ID不能为空");
        }
        // 校验设备是否存在
        deviceRepository.findById(deviceId)
                .orElseThrow(() -> new BusinessException("设备不存在或已删除"));
        log.info("根据设备ID查询告警记录：deviceId={}", deviceId);
        // 自定义查询：根据设备ID查询并按触发时间倒序
        return alarmRepository.findAll().stream()
                .filter(alarm -> alarm.getDeviceId().equals(deviceId))
                .sorted((a1, a2) -> a2.getTriggerTime().compareTo(a1.getTriggerTime()))
                .collect(Collectors.toList());
    }

    /**
     * 批量删除告警记录
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteAlarms(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException("请选择要删除的告警记录");
        }
        log.info("批量删除告警记录：ids={}", ids);
        // 校验告警记录是否存在
        List<Alarm> alarms = alarmRepository.findAllById(ids);
        if (alarms.size() != ids.size()) {
            throw new BusinessException("部分告警记录不存在或已删除");
        }
        // 执行删除
        alarmRepository.deleteAllById(ids);
    }
}