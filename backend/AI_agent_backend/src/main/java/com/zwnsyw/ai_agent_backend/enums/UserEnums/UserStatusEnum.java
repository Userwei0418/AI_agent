package com.zwnsyw.ai_agent_backend.enums.UserEnums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;


public enum UserStatusEnum {
    ACTIVE("ACTIVE", "正常"),
    DISABLED("DISABLED", "禁用");

    @EnumValue
    @Getter
    private final String code;

    @JsonValue
    @Getter
    private final String description;

    // 静态缓存以提高 fromCode 的查询效率
    private static final Map<String, UserStatusEnum> CODE_MAP = Arrays.stream(UserStatusEnum.values())
            .collect(Collectors.toMap(UserStatusEnum::getCode, status -> status));

    UserStatusEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static UserStatusEnum fromCode(String code) {
        if (code == null) {
            throw new IllegalArgumentException("用户状态代码不能为空");
        }

        UserStatusEnum status = CODE_MAP.get(code);
        if (status == null) {
            throw new IllegalArgumentException(
                    String.format("未知的用户状态代码：%s，合法的状态代码有：%s", code, CODE_MAP.keySet())
            );
        }

        return status;
    }
}
