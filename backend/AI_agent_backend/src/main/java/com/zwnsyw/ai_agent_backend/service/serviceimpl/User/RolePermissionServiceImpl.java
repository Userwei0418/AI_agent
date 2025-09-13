package com.zwnsyw.ai_agent_backend.service.serviceimpl.User;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zwnsyw.ai_agent_backend.entity.User.Permission;
import com.zwnsyw.ai_agent_backend.entity.User.Role;
import com.zwnsyw.ai_agent_backend.entity.User.RolePermission;
import com.zwnsyw.ai_agent_backend.mapper.User.PermissionMapper;
import com.zwnsyw.ai_agent_backend.mapper.User.RoleMapper;
import com.zwnsyw.ai_agent_backend.mapper.User.RolePermissionMapper;
import com.zwnsyw.ai_agent_backend.service.User.RolePermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class RolePermissionServiceImpl extends ServiceImpl<RolePermissionMapper, RolePermission> implements RolePermissionService {
    @Autowired
    private RolePermissionMapper rolePermissionMapper;

    @Autowired
    private PermissionMapper permissionMapper;

    @Autowired
    private RoleMapper roleMapper;
    @Override
    public Set<String> getPermissionsByRoleId(Long roleId) {
        List<RolePermission> rolePermissions = rolePermissionMapper.selectList(new QueryWrapper<RolePermission>().eq("roleId", roleId));
        Set<String> permissions = new HashSet<>();
        for (RolePermission rolePermission : rolePermissions) {
            Permission permission = permissionMapper.selectById(rolePermission.getPermissionId());
            if (permission != null) {
                permissions.add(permission.getCode());
            }
        }
        return permissions;
    }

    @Override
    public Role getRoleById(Long roleId) {
        return roleMapper.selectById(roleId);
    }
}
