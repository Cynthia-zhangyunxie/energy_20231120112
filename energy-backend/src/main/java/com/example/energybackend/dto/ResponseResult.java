package com.example.energybackend.dto;



import lombok.Data;

@Data
public class ResponseResult<T> {
    private int code;   // 状态码（200=成功，其他=失败）
    private String msg; // 提示信息
    private T data;     // 响应数据

    // 1. 无参构造函数（必须保留，Lombok的@Data默认生成）
    public ResponseResult() {}

    // 2. 带3个参数的构造函数（用于success/error方法）
    public ResponseResult(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    // 成功响应（无数据）
    public static <T> ResponseResult<T> success() {
        return new ResponseResult<>(200, "操作成功", null);
    }

    // 成功响应（有数据）
    public static <T> ResponseResult<T> success(T data) {
        return new ResponseResult<>(200, "操作成功", data);
    }

    // 失败响应
    public static <T> ResponseResult<T> error(int code, String msg) {
        return new ResponseResult<>(code, msg, null);
    }
}