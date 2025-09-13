package com.zwnsyw.ai_agent_backend.controller.User;

import com.zwnsyw.ai_agent_backend.RBAC.annotation.PreAuthorizeRole;
import com.zwnsyw.ai_agent_backend.common.response.BaseResponse;
import com.zwnsyw.ai_agent_backend.common.response.ResultUtils;
import com.zwnsyw.ai_agent_backend.entity.User.RolePermission;
import com.zwnsyw.ai_agent_backend.service.User.RolePermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/role-permissions")
public class RolePermissionController {
    @Autowired
    private RolePermissionService rolePermissionService;

    @GetMapping
    @PreAuthorizeRole("ADMIN")
    public BaseResponse<List<RolePermission>> getAllRolePermissions() {
        return ResultUtils.success(rolePermissionService.list());
    }

    @PostMapping
    @PreAuthorizeRole("ADMIN")
    public BaseResponse<RolePermission> createRolePermission(@RequestBody RolePermission rolePermission) {
        rolePermissionService.save(rolePermission);
        return ResultUtils.success(rolePermission);
    }

    @DeleteMapping("/{roleId}/{permissionId}")
    @PreAuthorizeRole("ADMIN")
    public BaseResponse<Boolean> deleteRolePermission(@PathVariable Long roleId, @PathVariable Long permissionId) {
        rolePermissionService.removeById(new RolePermission(roleId, permissionId));
        return ResultUtils.success(true);
    }
}
