package com.zwnsyw.ai_agent_backend.service.User;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zwnsyw.ai_agent_backend.entity.User.UserRole;

import java.util.List;

public interface UserRoleService extends IService<UserRole> {
    List<UserRole> getUserRolesByUserId(Long userId);
}
