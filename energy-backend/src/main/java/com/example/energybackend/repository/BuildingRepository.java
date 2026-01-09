package com.example.energybackend.repository;



import com.example.energybackend.entity.Building;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BuildingRepository extends JpaRepository<Building, Long> {
    // 查询未删除的建筑列表
    List<Building> findByIsDeletedOrderByCreateTimeDesc(Integer isDeleted);
}