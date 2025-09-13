package com.zwnsyw.ai_agent_backend.enums;

public interface BaseEnum<T> {
    T getCode(); // 返回枚举类存储的数据库值
}