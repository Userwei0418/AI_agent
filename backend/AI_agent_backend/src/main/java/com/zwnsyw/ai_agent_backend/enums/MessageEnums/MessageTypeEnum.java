package com.zwnsyw.ai_agent_backend.enums.MessageEnums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

@Getter
public enum MessageTypeEnum {
    REVIEW_MESSAGE("审核通知", 0),
    SYSTEM_MESSAGE("系统通知", 1),
    OTHER_MESSAGE("其他通知", 2);

    private final String text;
    private final int value;

    MessageTypeEnum(String text, int value) {
        this.text = text;
        this.value = value;
    }

    public static MessageTypeEnum getEnumByValue(Integer value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        for (MessageTypeEnum type : values()) {
            if (type.value == value) {
                return type;
            }
        }
        return null;
    }
}
