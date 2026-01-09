package com.example.energybackend.controller;



import com.example.energybackend.dto.ResponseResult;
import com.example.energybackend.entity.Building;
import com.example.energybackend.service.BuildingService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 建筑管理控制器
 */
@RestController
@RequestMapping("/building")
@Api(tags = "建筑管理接口")
public class BuildingController {

    @Autowired
    private BuildingService buildingService;

    @PostMapping("/add")
    @ApiOperation("新增建筑（仅管理员）")
    @RequiresRoles("ADMIN") // 仅管理员可操作
    public ResponseResult<Void> addBuilding(@Validated @RequestBody Building building) {
        buildingService.addBuilding(building);
        return ResponseResult.success();
    }

    @GetMapping("/list")
    @ApiOperation("查询所有建筑（登录用户均可访问）")
    public ResponseResult<List<Building>> getAllBuildings() {
        return ResponseResult.success(buildingService.getAllBuildings());
    }

    @GetMapping("/{id}")
    @ApiOperation("根据ID查询建筑")
    public ResponseResult<Building> getBuildingById(@PathVariable Long id) {
        return ResponseResult.success(buildingService.getBuildingById(id));
    }

    @PutMapping("/update")
    @ApiOperation("修改建筑信息（仅管理员）")
    @RequiresRoles("ADMIN")
    public ResponseResult<Void> updateBuilding(@Validated @RequestBody Building building) {
        buildingService.updateBuilding(building);
        return ResponseResult.success();
    }

    @DeleteMapping("/delete/{id}")
    @ApiOperation("删除建筑（仅管理员）")
    @RequiresRoles("ADMIN")
    public ResponseResult<Void> deleteBuilding(@PathVariable Long id) {
        buildingService.deleteBuilding(id);
        return ResponseResult.success();
    }
}