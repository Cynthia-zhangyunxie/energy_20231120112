package com.example.energybackend.controller;



import com.example.energybackend.dto.EnergyDataDTO;
import com.example.energybackend.dto.ResponseResult;
import com.example.energybackend.service.EnergyDataService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;




/**
 * 能耗数据控制器
 */
@RestController
@RequestMapping("/energy")
@Api(tags = "能耗数据接口")
public class EnergyDataController {

    @Autowired
    private EnergyDataService energyDataService;

    @GetMapping("/latest/{deviceId}")
    @ApiOperation("查询设备最新能耗数据")
    public ResponseResult<EnergyDataDTO> getLatestEnergyData(@PathVariable Long deviceId) {
        return ResponseResult.success(energyDataService.getLatestEnergyData(deviceId));
    }

    @GetMapping("/recent/{deviceId}")
    @ApiOperation("查询设备最近N条能耗数据")
    public ResponseResult<List<EnergyDataDTO>> getRecentEnergyData(
            @PathVariable Long deviceId,
            @RequestParam(defaultValue = "10") Integer n
    ) {
        return ResponseResult.success(energyDataService.getRecentEnergyData(deviceId, n));
    }

    @GetMapping("/today/building/{buildingId}")
    @ApiOperation("查询建筑今日用电量")
    public ResponseResult<Double> getTodayEnergyByBuilding(@PathVariable Long buildingId) {
        return ResponseResult.success(energyDataService.getTodayEnergyByBuilding(buildingId));
    }
}