package com.example.energybackend.repository;



import com.example.energybackend.entity.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {
    // 根据SN查询设备（用于校验SN唯一性）
    Optional<Device> findBySnAndIsDeleted(String sn, Integer isDeleted);

    // 根据房间 号查询未删除的设备（用于校验房间 设备唯一性）
    Optional<Device> findByRoomNoAndIsDeleted(String roomNo, Integer isDeleted);

    // 查询未删除的设备列表（按建筑ID分组）
    List<Device> findByIsDeletedOrderByBuildingIdAsc(Integer isDeleted);

    // 根据建筑ID查询未删除的设备
    List<Device> findByBuildingIdAndIsDeleted(Long buildingId, Integer isDeleted);
}