package com.zwnsyw.ai_agent_backend.utils;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PasswordUtils {

    // 定义静态盐值，仅供内部使用
    @Value("${security.salt}")
    private String STATIC_SALT;

    /**
     * 使用动态盐值和静态盐对密码进行加密
     *
     * @param password    用户输入的明文密码
     * @param dynamicSalt 动态盐值
     * @return 加密后的密码
     */
    public String encryptPassword(String password, String dynamicSalt) {
        // 拼接静态盐、动态盐和明文密码
        String enhancedPassword = STATIC_SALT + dynamicSalt + password;
        // 使用 BCrypt 加密
        return BCrypt.hashpw(enhancedPassword, BCrypt.gensalt());
    }

    /**
     * 验证密码是否正确
     *
     * @param rawPassword    用户输入的明文密码
     * @param storedPassword 数据库中存储的加密密码
     * @param dynamicSalt    数据库中存储的动态盐值
     * @return 是否匹配
     */
    public  boolean verifyPassword(String rawPassword, String storedPassword, String dynamicSalt) {
        // 拼接静态盐、动态盐和明文密码
        String enhancedPassword = STATIC_SALT + dynamicSalt + rawPassword;
        // 使用 BCrypt 进行验证
        return BCrypt.checkpw(enhancedPassword, storedPassword);
    }
}