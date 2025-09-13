package com.zwnsyw.ai_agent_backend.service.User;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zwnsyw.ai_agent_backend.entity.User.Role;
import com.zwnsyw.ai_agent_backend.entity.User.RolePermission;

import java.util.Set;

public interface RolePermissionService extends IService<RolePermission> {

    Set<String> getPermissionsByRoleId(Long roleId);

    Role getRoleById(Long roleId);
}
