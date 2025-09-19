package com.zwnsyw.ai_agent_backend.common.response;

import com.zwnsyw.ai_agent_backend.exception.ErrorCode;

public class ResultUtils {

    /**
     * 成功响应
     *
     * @param data 返回的数据
     * @param <T>  数据类型
     * @return 返回成功响应对象
     */
    public static <T> BaseResponse<T> success(T data) {
        // 如果数据是 Long 类型，则转换为 String 类型
        if (data instanceof Long) {
            data = (T) String.valueOf(data);
        }

        return new BaseResponse<T>()
                .code(ErrorCode.SUCCESS.getCode())
                .data(data)
                .message(ErrorCode.SUCCESS.getMessage())
                .description(ErrorCode.SUCCESS.getDescription())
                .timestamp(System.currentTimeMillis());
    }

    /**
     * 失败响应
     *
     * @param errorCode 错误码
     * @return 返回失败响应对象
     */
    public static BaseResponse error(ErrorCode errorCode) {
        return new BaseResponse<>()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .description(errorCode.getDescription())
                .timestamp(System.currentTimeMillis());
    }

    /**
     * 自定义失败响应
     *
     * @param code        错误码
     * @param message     错误信息
     * @param description 错误描述
     * @return 返回自定义失败响应对象
     */
    public static BaseResponse error(int code, String message, String description) {
        return new BaseResponse<>()
                .code(code)
                .message(message)
                .description(description)
                .timestamp(System.currentTimeMillis());
    }

    /**
     * 自定义失败响应（接收 ErrorCode）
     *
     * @param errorCode 错误码
     * @param description 错误描述
     * @return 返回失败响应对象
     */
    public static BaseResponse error(ErrorCode errorCode, String description) {
        return new BaseResponse<>()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .description(description)
                .timestamp(System.currentTimeMillis());
    }
}
