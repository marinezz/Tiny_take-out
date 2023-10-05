package com.sky.common.annotation;


import com.sky.common.enumeration.OperationType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义注解，用于标识某个方法需要进行功能字段的自动填充
 * @Target(ElementType.METHOD) 说明只能标注在方法上
 * @Retention(RetentionPolicy.RUNTIME)注解的生命周期，注解不仅被保存到class文件中，jvm加载class文件之后，仍然存在；
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoFill {
    // 数据库操作类型
    OperationType value();
}
