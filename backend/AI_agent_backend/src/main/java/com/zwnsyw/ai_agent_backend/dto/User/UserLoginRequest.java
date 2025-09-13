package com.zwnsyw.ai_agent_backend.dto.User;

import com.zwnsyw.ai_agent_backend.dto.utils.ValidUserAccount;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;


import java.io.Serializable;

@Data
public class UserLoginRequest implements Serializable {
    private static final long serialVersionUID = 3191241716373120793L;

    /**
     * 账号
     */
    @NotBlank(message = "账号不能为空")
    @Size(max = 20, message = "账号长度不能超过20")
    @ValidUserAccount
    private String userAccount;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    @Size(min = 8, max = 20, message = "密码长度必须在8到20之间")
    private String userPassword;
}

