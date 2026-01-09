package com.example.energybackend.service;


import com.example.energybackend.entity.Alarm;
import java.util.List;

/**
 * 告警记录业务逻辑接口
 */
public interface AlarmService {
    /**
     * 查询所有告警记录（按触发时间倒序）
     * @return 告警记录列表
     */
    List<Alarm> getAllAlarms();

    /**
     * 根据设备SN模糊查询告警记录
     * @param sn 设备序列号（支持模糊匹配）
     * @return 匹配的告警记录列表
     */
    List<Alarm> searchAlarmsBySn(String sn);

    /**
     * 根据设备ID查询告警记录
     * @param deviceId 设备主键ID
     * @return 该设备的所有告警记录
     */
    List<Alarm> getAlarmsByDeviceId(Long deviceId);

    /**
     * 批量删除告警记录（仅管理员可操作）
     * @param ids 告警记录ID列表
     */
    void batchDeleteAlarms(List<Long> ids);
}