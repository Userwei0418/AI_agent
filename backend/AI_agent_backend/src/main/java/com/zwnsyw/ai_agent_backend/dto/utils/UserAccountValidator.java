package com.zwnsyw.ai_agent_backend.dto.utils;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

public class UserAccountValidator implements ConstraintValidator<ValidUserAccount, String> {

    // 更新正则表达式，确保包含所有特殊字符
    private static final String INVALID_PATTERN = ".*[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：\"\"'。，、？].*";

    @Override
    public void initialize(ValidUserAccount constraintAnnotation) {
        // 初始化配置
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // 空值交给@NotBlank等注解处理
        if (value == null || value.isEmpty()) {
            return true;
        }

        // 检查是否包含特殊字符
        return !Pattern.matches(INVALID_PATTERN, value);
    }
}
