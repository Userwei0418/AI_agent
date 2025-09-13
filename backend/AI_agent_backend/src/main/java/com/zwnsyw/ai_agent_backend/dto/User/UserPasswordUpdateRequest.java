package com.zwnsyw.ai_agent_backend.dto.User;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;




@Data
public class UserPasswordUpdateRequest {
    /**
     * 旧密码：用户必须提供当前密码用于确认身份
     */
    @NotBlank(message = "旧密码不能为空")
    @Size(min = 8, max = 20, message = "旧密码长度必须在8到20位之间")
    private String oldPassword;

    /**
     * 新密码：用户设置的新密码
     * todo 密码强度检测
     */
    @NotBlank(message = "新密码不能为空")
    @Size(min = 8, max = 20, message = "新密码长度必须在8到20位之间")
    private String newPassword;
}
