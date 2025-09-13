package com.zwnsyw.ai_agent_backend.dto.utils;


import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;
import java.util.regex.Pattern;

public class ValidTagsValidator implements ConstraintValidator<ValidTags, List<String>> {

    private static final Pattern TAG_PATTERN = Pattern.compile("^[a-zA-Z0-9\u4e00-\u9fa5]+$");

    @Override
    public boolean isValid(List<String> tags, ConstraintValidatorContext context) {
        if (tags == null) {
            return false;
        }
        for (String tag : tags) {
            if (tag == null || tag.trim().isEmpty()) {
                return false;
            }
            if (tag.length() > 20) {
                return false;
            }
            if (!TAG_PATTERN.matcher(tag).matches()) {
                return false;
            }
        }
        return true;
    }
}
