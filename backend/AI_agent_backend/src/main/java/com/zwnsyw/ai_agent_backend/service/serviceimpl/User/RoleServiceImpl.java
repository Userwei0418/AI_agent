package com.zwnsyw.ai_agent_backend.service.serviceimpl.User;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zwnsyw.ai_agent_backend.entity.User.Role;
import com.zwnsyw.ai_agent_backend.entity.User.UserRole;
import com.zwnsyw.ai_agent_backend.mapper.User.RoleMapper;
import com.zwnsyw.ai_agent_backend.service.User.RolePermissionService;
import com.zwnsyw.ai_agent_backend.service.User.RoleService;
import com.zwnsyw.ai_agent_backend.service.User.UserRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {
    @Autowired
    private UserRoleService userRoleService;

    @Autowired
    private RolePermissionService rolePermissionService;

    @Override
    public List<Role> getRolesByUserId(Long userId) {
        List<UserRole> userRoles = userRoleService.getUserRolesByUserId(userId);
        return userRoles.stream()
                .map(userRole -> getById(userRole.getRoleId()))
                .collect(Collectors.toList());
    }

    @Override
    public Set<String> getRolePermissionByUserId(Long userId) {
        List<UserRole> userRoles = userRoleService.getUserRolesByUserId(userId);
        Set<String> permissions = new HashSet<>();
        for (UserRole userRole : userRoles) {
            Set<String> rolePermissions = rolePermissionService.getPermissionsByRoleId(userRole.getRoleId());
            permissions.addAll(rolePermissions);
        }
        return permissions;
    }
}