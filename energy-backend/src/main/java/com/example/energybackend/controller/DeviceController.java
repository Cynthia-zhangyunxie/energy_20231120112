package com.example.energybackend.controller;


import com.example.energybackend.dto.DeviceDTO;
import com.example.energybackend.dto.ResponseResult;
import com.example.energybackend.entity.Device;
import com.example.energybackend.service.DeviceService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import org.apache.shiro.authz.annotation.RequiresRoles;

import org.springframework.web.bind.annotation.*;


/**
 * 设备管理控制器
 */
@RestController
@RequestMapping("/device")
@Api(tags = "设备管理接口")
public class DeviceController {

    @Autowired
    private DeviceService deviceService;

    @PostMapping("/add")
    @ApiOperation("新增设备（仅管理员）")
    @RequiresRoles("ADMIN")
    public ResponseResult<Void> addDevice(@Validated @RequestBody DeviceDTO deviceDTO) {
        deviceService.addDevice(deviceDTO);
        return ResponseResult.success();
    }

    @GetMapping("/list")
    @ApiOperation("查询所有设备（登录用户均可访问）")
    public ResponseResult<List<Device>> getAllDevices() {
        return ResponseResult.success(deviceService.getAllDevices());
    }

    @GetMapping("/{id}")
    @ApiOperation("根据ID查询设备")
    public ResponseResult<Device> getDeviceById(@PathVariable Long id) {
        return ResponseResult.success(deviceService.getDeviceById(id));
    }

    @PostMapping("/replace/{oldDeviceId}")
    @ApiOperation("更换设备（仅管理员）")
    @RequiresRoles("ADMIN")
    public ResponseResult<Void> replaceDevice(
            @PathVariable Long oldDeviceId,
            @Validated @RequestBody DeviceDTO newDeviceDTO
    ) {
        deviceService.replaceDevice(oldDeviceId, newDeviceDTO);
        return ResponseResult.success();
    }

    @GetMapping("/building/{buildingId}")
    @ApiOperation("根据建筑ID查询设备")
    public ResponseResult<List<Device>> getDevicesByBuildingId(@PathVariable Long buildingId) {
        return ResponseResult.success(deviceService.getDevicesByBuildingId(buildingId));
    }
}