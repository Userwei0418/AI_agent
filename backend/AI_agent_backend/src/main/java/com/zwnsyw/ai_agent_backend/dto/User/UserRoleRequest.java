package com.zwnsyw.ai_agent_backend.dto.User;

import jakarta.validation.constraints.Pattern;
import lombok.Data;



@Data
public class UserRoleRequest {

    private Long userId;

    @Pattern(regexp = "^(USER|VIP|ADMIN)$",
            message = "roleName字段值不合法，允许的值为 USER|VIP|ADMIN")
    private String roleName;

}
