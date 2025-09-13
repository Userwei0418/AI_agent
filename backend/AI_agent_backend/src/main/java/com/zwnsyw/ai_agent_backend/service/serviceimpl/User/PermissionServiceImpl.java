package com.zwnsyw.ai_agent_backend.service.serviceimpl.User;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zwnsyw.ai_agent_backend.entity.User.Permission;
import com.zwnsyw.ai_agent_backend.entity.User.UserRole;
import com.zwnsyw.ai_agent_backend.enums.UserEnums.UserRoleEnum;
import com.zwnsyw.ai_agent_backend.mapper.User.PermissionMapper;
import com.zwnsyw.ai_agent_backend.service.User.PermissionService;
import com.zwnsyw.ai_agent_backend.service.User.RolePermissionService;
import com.zwnsyw.ai_agent_backend.service.User.UserRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class PermissionServiceImpl extends ServiceImpl<PermissionMapper, Permission> implements PermissionService {

    @Autowired
    private UserRoleService userRoleService;

    @Autowired
    private RolePermissionService rolePermissionService;

    @Override
    public Set<String> getMenuPermission(Long userId) {
        Set<String> perms = new HashSet<>();

        // 获取用户的角色
        List<UserRole> userRoles = userRoleService.getUserRolesByUserId(userId);

        // 管理员拥有所有权限
        boolean isAdmin = userRoles.stream()
                .anyMatch(userRole -> rolePermissionService.getRoleById(userRole.getRoleId()).getName() == UserRoleEnum.ADMIN);
        if (isAdmin) {
            perms.add("*:*:*");
        } else {
            for (UserRole userRole : userRoles) {
                Set<String> rolePerms = rolePermissionService.getPermissionsByRoleId(userRole.getRoleId());
                perms.addAll(rolePerms);
            }
        }

        return perms;
    }

}
