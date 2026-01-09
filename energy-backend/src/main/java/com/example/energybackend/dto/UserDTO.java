package com.example.energybackend.dto;


import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 用户数据传输对象（用于用户信息新增、修改、展示）
 */
@Data
public class UserDTO {
    private Long id; // 主键ID（新增时为空，修改时必填）

    @NotBlank(message = "用户名不能为空")
    @Length(min = 3, max = 20, message = "用户名长度需在3-20字符之间")
    private String username; // 用户名（唯一）

    @NotBlank(message = "密码不能为空", groups = AddGroup.class) // 新增时必填
    @Length(min = 6, max = 20, message = "密码长度需在6-20字符之间")
    private String password; // 密码（新增时传入明文，后端加密存储；修改时可选）

    @NotBlank(message = "角色不能为空")
    private String role; // 角色（ADMIN/NORMAL）

    private String nickname; // 昵称（可选）

    @NotNull(message = "用户状态不能为空")
    private Integer status; // 状态（1-正常，0-禁用）

    // 分组校验：区分新增和修改场景
    public interface AddGroup {}
    public interface UpdateGroup {}
}