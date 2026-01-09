package com.example.energybackend.repository;



import com.example.energybackend.entity.EnergyData;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EnergyDataRepository extends JpaRepository<EnergyData, Long> {
    // 查询设备最新一条能耗数据
    List<EnergyData> findTop1ByDeviceIdOrderByCollectTimeDesc(Long deviceId);

    // 动态查询指定设备的前N条数据（按collectTime降序）
    List<EnergyData> findByDeviceIdOrderByCollectTimeDesc(Long deviceId, Pageable pageable);

    // 查询建筑今日用电量总和（用于饼图）
    @Query("SELECT SUM(ed.totalEnergy - COALESCE(prev.totalEnergy, 0)) " +
            "FROM EnergyData ed " +
            "LEFT JOIN Device d ON ed.deviceId = d.id " +
            "LEFT JOIN EnergyData prev ON prev.deviceId = ed.deviceId " +
            "AND prev.collectTime = (SELECT MIN(collectTime) FROM EnergyData WHERE deviceId = ed.deviceId AND DATE(collectTime) = CURRENT_DATE) " +
            "WHERE d.buildingId = :buildingId " +
            "AND DATE(ed.collectTime) = CURRENT_DATE " +
            "GROUP BY ed.deviceId")
    List<Double> calculateTodayEnergyByBuilding(@Param("buildingId") Long buildingId);
}