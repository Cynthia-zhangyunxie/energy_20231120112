package com.example.energybackend.service;



import com.example.energybackend.entity.Building;

import java.util.List;

/**
 * 建筑管理业务逻辑接口
 */
public interface BuildingService {
    /**
     * 查询所有未删除的建筑
     * @return 建筑列表
     */
    List<Building> getAllBuildings();

    /**
     * 根据ID查询建筑
     * @param id 建筑ID
     * @return 建筑实体
     */
    Building getBuildingById(Long id);

    /**
     * 新增建筑
     * @param building 建筑实体
     */
    void addBuilding(Building building);

    /**
     * 修改建筑信息
     * @param building 建筑实体
     */
    void updateBuilding(Building building);

    /**
     * 删除建筑（逻辑删除）
     * @param id 建筑ID
     */
    void deleteBuilding(Long id);
}