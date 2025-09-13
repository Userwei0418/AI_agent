package com.zwnsyw.ai_agent_backend.common.response;

import lombok.Data;

import java.io.Serializable;

@Data
public class BaseResponse<T> implements Serializable {

    /**
     * 状态码
     */
    private int code;

    /**
     * 数据
     */
    private T data;

    /**
     * 消息
     */
    private String message;

    /**
     * 描述
     */
    private String description;

    /**
     * 响应时间戳
     */
    private long timestamp;

    // 使用链式调用来简化重载
    public BaseResponse<T> code(int code) {
        this.code = code;
        return this;
    }

    public BaseResponse<T> data(T data) {
        this.data = data;
        return this;
    }

    public BaseResponse<T> message(String message) {
        this.message = message;
        return this;
    }

    public BaseResponse<T> description(String description) {
        this.description = description;
        return this;
    }

    public BaseResponse<T> timestamp(long timestamp) {
        this.timestamp = timestamp;
        return this;
    }
}
