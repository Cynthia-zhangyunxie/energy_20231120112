package com.example.energybackend.repository;


import com.example.energybackend.entity.Alarm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlarmRepository extends JpaRepository<Alarm, Long> {
    // 根据设备SN查询告警记录（关联设备表）
    @Query("SELECT a FROM Alarm a JOIN Device d ON a.deviceId = d.id WHERE d.sn LIKE %:sn% ORDER BY a.triggerTime DESC")
    List<Alarm> findByDeviceSnLike(@Param("sn") String sn);

    // 查询所有告警记录（按触发时间倒序）
    List<Alarm> findAllByOrderByTriggerTimeDesc();
}