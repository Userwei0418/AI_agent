package com.zwnsyw.ai_agent_backend.service.User;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zwnsyw.ai_agent_backend.entity.User.Role;

import java.util.List;
import java.util.Set;

public interface RoleService extends IService<Role> {
    List<Role> getRolesByUserId(Long userId);

    Set<String> getRolePermissionByUserId(Long userId);
}
