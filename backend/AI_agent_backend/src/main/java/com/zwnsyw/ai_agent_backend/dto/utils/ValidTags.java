package com.zwnsyw.ai_agent_backend.dto.utils;


import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidTagsValidator.class)
@Documented
public @interface ValidTags {
    String message() default "标签包含非法字符或空值";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
