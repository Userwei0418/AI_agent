package com.zwnsyw.ai_agent_backend.dto.User;

import com.zwnsyw.ai_agent_backend.dto.utils.ValidUserAccount;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;


import java.io.Serializable;

@Data
public class UserRegisterRequest implements Serializable {
    private static final long serialVersionUID = 3191241716373120793L;
    /**
     * 用户账号
     */
    @NotBlank(message = "账号不能为空")
    @Size(max = 20, message = "账号长度不能超过20")
    @ValidUserAccount
    private String userAccount;

    /**
     * 用户密码
     * todo 密码强度检测
     */
    @NotBlank(message = "密码不能为空")
    @Size(min = 8, max = 20, message = "密码长度必须在8到20之间")
    private String userPassword;

    /**
     * 校验密码
     */
    @NotBlank(message = "重复密码不能为空")
    @Size(min = 8, max = 20, message = "重复密码长度必须在8到20之间")
    private String checkPassword;

    /**
     * 邀请码 （可选项）
     */
    private String inviteCode;

    /**
     * 确认密码
     */
    @AssertTrue(message = "密码和确认密码不一致")
    public boolean isPasswordMatching() {
        // 添加对空值的检查
        if (this.userPassword == null || this.checkPassword == null) {
            return false;
        }
        // 确保密码不为空
        if (this.userPassword.isEmpty() || this.checkPassword.isEmpty()) {
            return false;
        }
        return this.userPassword.equals(this.checkPassword);
    }
}