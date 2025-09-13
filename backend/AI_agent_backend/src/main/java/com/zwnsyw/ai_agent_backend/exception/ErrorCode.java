package com.zwnsyw.ai_agent_backend.exception;

public enum ErrorCode {
    SUCCESS(0, "success", "", ErrorType.SUCCESS),
    PARAMS_ERROR(40000, "请求参数错误", "", ErrorType.CLIENT),
    NULL_ERROR(40001, "请求数据为空", "", ErrorType.CLIENT),
    NOT_LOGIN_ERROR(40100, "未登录", "", ErrorType.CLIENT),
    NO_AUTH_ERROR(40101, "无权限", "", ErrorType.CLIENT),
    FORBIDDEN_ERROR(40300, "禁止访问", "", ErrorType.CLIENT),
    NOT_FOUND_ERROR(40400, "请求数据不存在","",ErrorType.SERVER),
    SYSTEM_ERROR(50000, "系统内部异常", "", ErrorType.SERVER),
    OPERATION_ERROR(50001, "操作失败","",ErrorType.SERVER);

    private final int code;
    private final String message;
    private final String description;
    private final ErrorType type;

    public enum ErrorType {
        CLIENT, SERVER, SUCCESS;
    }

    ErrorCode(int code, String message, String description, ErrorType type) {
        this.code = code;
        this.message = message;
        this.description = description;
        this.type = type;
    }

    public int getCode() { return code; }
    public String getMessage() { return message; }
    public String getDescription() { return description; }
    public ErrorType getType() { return type; }

    public String getFullMessage() {
        return "Code: " + code + ", Message: " + message + ", Description: " + description;
    }
}