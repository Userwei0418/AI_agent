package com.zwnsyw.ai_agent_backend.exception;

public class BusinessException extends RuntimeException {
    private final int code;
    private final String description;
    private final ErrorCode.ErrorType errorType;

    public BusinessException(String message, int code, String description, ErrorCode.ErrorType errorType) {
        super(message);
        this.code = code;
        this.description = description;
        this.errorType = errorType;
    }

    public BusinessException(ErrorCode errorCode) {
        this(errorCode.getMessage(), errorCode.getCode(), errorCode.getDescription(), errorCode.getType());
    }

    public BusinessException(ErrorCode errorCode, String description) {
        this(errorCode.getMessage(), errorCode.getCode(), description, errorCode.getType());
    }

    public BusinessException(ErrorCode errorCode, String description, ErrorCode.ErrorType errorType) {
        this(errorCode.getMessage(), errorCode.getCode(), description, errorType);
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public ErrorCode.ErrorType getErrorType() {
        return errorType;
    }

    @Override
    public String toString() {
        return String.format("BusinessException [code=%d, message=%s, description=%s, type=%s]",
                code, getMessage(), description, errorType);
    }
}
