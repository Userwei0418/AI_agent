package com.zwnsyw.ai_agent_backend.dto.utils;


import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UserAccountValidator.class)
public @interface ValidUserAccount {
    String message() default "账号包含特殊字符";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
