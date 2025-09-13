package com.zwnsyw.ai_agent_backend.service.User;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zwnsyw.ai_agent_backend.entity.User.Permission;

import java.util.Set;

public interface PermissionService extends IService<Permission> {

    Set<String> getMenuPermission(Long userId);

}
