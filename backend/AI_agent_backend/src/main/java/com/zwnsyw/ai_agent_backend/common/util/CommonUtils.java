package com.zwnsyw.ai_agent_backend.common.util;

import java.util.UUID;

public class CommonUtils {

    /**
     * 生成唯一UUID（无横线）
     */
    public static String generateUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 校验手机号格式
     */
    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.isEmpty()) {
            return false;
        }
        // 简单的手机号正则表达式
        return phone.matches("^1[3-9]\\d{9}$");
    }

    /**
     * 校验邮箱格式
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        // 简单的邮箱正则表达式
        return email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}$");
    }
}
