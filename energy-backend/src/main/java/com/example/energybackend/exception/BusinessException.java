package com.example.energybackend.exception;



// 业务异常类（用于处理可预见的业务逻辑错误）
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}