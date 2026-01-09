package com.example.energybackend.service.impl;



import com.example.energybackend.entity.Building;
import com.example.energybackend.exception.BusinessException;
import com.example.energybackend.repository.BuildingRepository;
import com.example.energybackend.service.BuildingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class BuildingServiceImpl implements BuildingService {

    @Autowired
    private BuildingRepository buildingRepository;

    @Override
    public List<Building> getAllBuildings() {
        log.info("查询所有未删除的建筑");
        return buildingRepository.findByIsDeletedOrderByCreateTimeDesc(0);
    }

    @Override
    public Building getBuildingById(Long id) {
        log.info("根据ID查询建筑：id={}", id);
        return buildingRepository.findById(id)
                .orElseThrow(() -> new BusinessException("建筑不存在或已删除"));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addBuilding(Building building) {
        log.info("新增建筑：name={}", building.getName());
        buildingRepository.save(building);
        log.info("新增建筑成功：id={}", building.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateBuilding(Building building) {
        log.info("修改建筑信息：id={}", building.getId());
        // 校验建筑是否存在
        getBuildingById(building.getId());
        buildingRepository.save(building);
        log.info("修改建筑信息成功：id={}", building.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBuilding(Long id) {
        log.info("删除建筑：id={}", id);
        // 1. 查询建筑
        Building building = getBuildingById(id);
        // 2. 逻辑删除（设置is_deleted=1）
        building.setIsDeleted(1);
        buildingRepository.save(building);
        log.info("删除建筑成功：id={}", id);
    }
}