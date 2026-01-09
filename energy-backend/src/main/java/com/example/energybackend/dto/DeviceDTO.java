package com.example.energybackend.dto;


import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class DeviceDTO {
    private Long id; // 主键ID（新增时为空，修改时必填）

    @NotBlank(message = "设备名称不能为空")
    @Length(max = 50, message = "设备名称长度不能超过50字符")
    private String name; // 设备名称

    @NotBlank(message = "设备序列号SN不能为空")
    @Length(max = 50, message = "设备序列号长度不能超过50字符")
    private String sn; // 唯一设备序列号

    @NotBlank(message = "通讯状态不能为空")
    private String status; // 通讯状态（ONLINE/OFFLINE/STOPPED）

    @NotNull(message = "额定功率阈值不能为空")
    private BigDecimal ratedPower; // 额定功率阈值（W）

    @NotNull(message = "所属建筑ID不能为空")
    private Long buildingId; // 外键_所属建筑ID

    @NotBlank(message = "所属房间号不能为空")
    @Length(max = 20, message = "房间号长度不能超过20字符")
    private String roomNo; // 所属房间号
}