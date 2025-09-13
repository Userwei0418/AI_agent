package com.zwnsyw.ai_agent_backend.exception;

import com.zwnsyw.ai_agent_backend.common.response.BaseResponse;
import com.zwnsyw.ai_agent_backend.common.response.ResultUtils;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Hidden
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 处理自定义业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public BaseResponse<?> handleBusinessException(BusinessException e, HttpServletRequest request) {
        logDetailedException(e, request);
        return generateErrorResponse(e.getErrorType(), e.getCode(), e.getMessage(), e.getDescription());
    }

    /**
     * 处理运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    public BaseResponse<?> handleRuntimeException(RuntimeException e, HttpServletRequest request) {
        logDetailedException(e, request);
        // 检查是否是包装的 BusinessExeception
        Throwable cause = e.getCause();
        if (cause != null && cause instanceof BusinessException) {
            return handleBusinessException((BusinessException) cause, request);
        }
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "系统异常");
    }

    /**
     * 处理DTO验证失败的异常(MethodArgumentNotValidException)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public BaseResponse<?> handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        BindingResult bindingResult = ex.getBindingResult();
        List<String> fieldErrors = bindingResult.getFieldErrors().stream()
                .map(error -> String.format("%s: %s", error.getField(), error.getDefaultMessage()))
                .collect(Collectors.toList());

        log.error("Validation exception at {}: Error details: {}",
                request.getRequestURI(),
                fieldErrors);

        return ResultUtils.error(ErrorCode.PARAMS_ERROR.getCode(), "请求参数校验失败", String.join(" | ", fieldErrors));
    }

    /**
     * 处理DTO验证失败的异常 (BindException)
     */
    @ExceptionHandler(BindException.class)
    public BaseResponse<?> handleBindException(BindException ex, HttpServletRequest request) {
        BindingResult bindingResult = ex.getBindingResult();
        List<String> fieldErrors = bindingResult.getFieldErrors().stream()
                .map(error -> String.format("%s: %s", error.getField(), error.getDefaultMessage()))
                .collect(Collectors.toList());

        log.error("Validation exception at {}: Error details: {}",
                request.getRequestURI(),
                fieldErrors);

        return ResultUtils.error(ErrorCode.PARAMS_ERROR.getCode(), "请求参数校验失败", String.join(" | ", fieldErrors));
    }

    /**
     * 处理 IllegalArgumentException 异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public BaseResponse<?> handleIllegalArgumentException(IllegalArgumentException e, HttpServletRequest request) {
        logDetailedException(e, request);
        return ResultUtils.error(ErrorCode.PARAMS_ERROR.getCode(), "参数错误", e.getMessage());
    }

    /**
     * 处理所有未捕获的异常
     */
    @ExceptionHandler(Exception.class)
    public BaseResponse<?> handleException(Exception e, HttpServletRequest request) {
        logDetailedException(e, request);
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "系统异常");
    }

    /**
     * 记录详细的异常日志信息
     */
    private void logDetailedException(Throwable e, HttpServletRequest request) {
        // 检查是否已经记录过该异常
        if (e instanceof BusinessException) {
            return;
        }
        StackTraceElement throwingElement = getThrowingElement(e);
        String clientIp = request.getRemoteAddr();
        String requestMethod = request.getMethod();

        log.error("Exception occurred: [Type={}, Message={}, URI={}, Method={}, IP={}, Class={}, Method={}, Line={}]",
                e.getClass().getSimpleName(),
                e.getMessage(),
                request.getRequestURI(),
                requestMethod,
                clientIp,
                throwingElement.getClassName(),
                throwingElement.getMethodName(),
                throwingElement.getLineNumber());
    }

    /**
     * 获取抛出异常的堆栈信息
     */
    private StackTraceElement getThrowingElement(Throwable e) {
        StackTraceElement[] stackTrace = e.getStackTrace();
        for (StackTraceElement element : stackTrace) {
            // 排除框架内部类和工具类（如ThrowUtils）
            if (!element.getClassName().startsWith("com.zwnsyw.ai_agent_backend.exception.ThrowUtils")) {
                return element;
            }
        }
        return (stackTrace.length > 0) ? stackTrace[0] : new StackTraceElement("UnknownClass", "UnknownMethod", "UnknownFile", -1);
    }

    /**
     * 生成错误响应
     */
    private BaseResponse<?> generateErrorResponse(ErrorCode.ErrorType errorType, int code, String message, String description) {
        switch (errorType) {
            case CLIENT:
                return ResultUtils.error(code, message, description);
            case SERVER:
                // 服务端异常可返回请求标识符，用于问题追踪
                String traceId = UUID.randomUUID().toString(); // 或从上下文获取 TraceId
                return ResultUtils.error(code, "服务器内部错误 (Trace ID: " + traceId + ")", "详情：" + description);
            default:
                return ResultUtils.error(code, "未知错误", description);
        }
    }
}