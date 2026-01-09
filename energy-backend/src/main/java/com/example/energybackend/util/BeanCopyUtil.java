package com.example.energybackend.util;



import org.springframework.beans.BeanUtils;

public class BeanCopyUtil {
    // 拷贝Bean属性（源对象 -> 目标对象）
    public static <T> T copyBean(Object source, Class<T> targetClass) {
        try {
            T target = targetClass.newInstance();
            BeanUtils.copyProperties(source, target);
            return target;
        } catch (Exception e) {
            throw new RuntimeException("Bean拷贝失败：" + e.getMessage());
        }
    }
}