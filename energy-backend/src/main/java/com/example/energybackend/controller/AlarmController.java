package com.example.energybackend.controller;



import com.example.energybackend.dto.ResponseResult;
import com.example.energybackend.entity.Alarm;
import com.example.energybackend.service.AlarmService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


import org.apache.shiro.authz.annotation.RequiresRoles;

import org.springframework.web.bind.annotation.DeleteMapping;

import org.springframework.web.bind.annotation.PathVariable;

import org.springframework.web.bind.annotation.RequestBody;




/**
 * 告警记录控制器
 */
@RestController
@RequestMapping("/alarm")
@Api(tags = "告警记录接口")
public class AlarmController {

    @Autowired
    private AlarmService alarmService;

    @GetMapping("/list")
    @ApiOperation("查询所有告警记录（登录用户均可访问）")
    public ResponseResult<List<Alarm>> getAllAlarms() {
        return ResponseResult.success(alarmService.getAllAlarms());
    }

    @GetMapping("/search")
    @ApiOperation("根据设备SN模糊查询告警记录")
    public ResponseResult<List<Alarm>> searchAlarmsBySn(@RequestParam String sn) {
        return ResponseResult.success(alarmService.searchAlarmsBySn(sn));
    }

    @GetMapping("/device/{deviceId}")
    @ApiOperation("根据设备ID查询告警记录")
    public ResponseResult<List<Alarm>> getAlarmsByDeviceId(@PathVariable Long deviceId) {
        return ResponseResult.success(alarmService.getAlarmsByDeviceId(deviceId));
    }

    @DeleteMapping("/batch")
    @ApiOperation("批量删除告警记录（仅管理员）")
    @RequiresRoles("ADMIN")
    public ResponseResult<Void> batchDeleteAlarms(@RequestBody List<Long> ids) {
        alarmService.batchDeleteAlarms(ids);
        return ResponseResult.success();
    }
}