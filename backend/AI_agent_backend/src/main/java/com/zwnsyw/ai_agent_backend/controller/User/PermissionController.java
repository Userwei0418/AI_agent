package com.zwnsyw.ai_agent_backend.controller.User;


import com.zwnsyw.ai_agent_backend.RBAC.annotation.PreAuthorizeRole;
import com.zwnsyw.ai_agent_backend.common.response.BaseResponse;
import com.zwnsyw.ai_agent_backend.common.response.ResultUtils;
import com.zwnsyw.ai_agent_backend.entity.User.Permission;
import com.zwnsyw.ai_agent_backend.service.User.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/permissions")
public class PermissionController {
    @Autowired
    private PermissionService permissionService;

    @GetMapping
    @PreAuthorizeRole("ADMIN")
    public BaseResponse<List<Permission>> getAllPermissions() {
        return ResultUtils.success(permissionService.list());
    }

    @PostMapping
    @PreAuthorizeRole("ADMIN")
    public BaseResponse<Permission> createPermission(@RequestBody Permission permission) {
        permissionService.save(permission);
        return ResultUtils.success(permission);
    }

    @PutMapping("/{permissionId}")
    @PreAuthorizeRole("ADMIN")
    public BaseResponse<Permission> updatePermission(@PathVariable Long permissionId, @RequestBody Permission permission) {
        permission.setId(permissionId);
        permissionService.updateById(permission);
        return ResultUtils.success(permission);
    }

    @DeleteMapping("/{permissionId}")
    @PreAuthorizeRole("ADMIN")
    public BaseResponse<Boolean> deletePermission(@PathVariable Long permissionId) {
        permissionService.removeById(permissionId);
        return ResultUtils.success(true);
    }
}
