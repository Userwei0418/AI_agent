package com.zwnsyw.ai_agent_backend.enums.UserEnums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import com.zwnsyw.ai_agent_backend.enums.BaseEnum;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum UserGenderEnum implements BaseEnum<String> {
    MALE("MALE", "男"),
    FEMALE("FEMALE", "女"),
    UNKNOWN("UNKNOWN", "保密");

    @EnumValue
    @Getter
    private final String code;

    @JsonValue
    @Getter
    private final String description;

    // 静态缓存，用于提升 fromCode 方法的效率
    private static final Map<String, UserGenderEnum> CODE_MAP = Arrays.stream(UserGenderEnum.values())
            .collect(Collectors.toMap(UserGenderEnum::getCode, gender -> gender));

    UserGenderEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    @Override
    public String getCode() {
        return code;
    }

    // 优化的静态方法，从码值获取对应枚举实例
    public static UserGenderEnum fromCode(String code) {
        if (code == null) {
            throw new IllegalArgumentException("性别代码不能为空");
        }

        UserGenderEnum gender = CODE_MAP.get(code);
        if (gender == null) {
            throw new IllegalArgumentException(
                    "未知的性别代码: " + code + "，可用的性别代码是: " + CODE_MAP.keySet()
            );
        }
        return gender;
    }
}