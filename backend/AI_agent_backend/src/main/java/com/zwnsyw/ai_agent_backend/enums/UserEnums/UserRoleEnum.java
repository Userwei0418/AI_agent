package com.zwnsyw.ai_agent_backend.enums.UserEnums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import com.zwnsyw.ai_agent_backend.enums.BaseEnum;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum UserRoleEnum implements BaseEnum<String> {
    USER("USER", "普通用户"),
    VIP("VIP", "会员"),
    ADMIN("ADMIN", "管理员");

    @EnumValue
    @Getter
    private final String code;

    @JsonValue
    @Getter
    private final String description;

    UserRoleEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    // 静态缓存以提高 fromCode 的查找效率
    private static final Map<String, UserRoleEnum> CODE_MAP = Arrays.stream(UserRoleEnum.values())
            .collect(Collectors.toMap(UserRoleEnum::getCode, role -> role));

    public static UserRoleEnum fromCode(String code) {
        if (code == null) {
            throw new IllegalArgumentException("角色代码不能为空");
        }

        UserRoleEnum role = CODE_MAP.get(code);
        if (role == null) {
            throw new IllegalArgumentException(
                    String.format("未知的用户角色代码：%s，合法的角色代码有：%s", code, CODE_MAP.keySet())
            );
        }

        return role;
    }
}