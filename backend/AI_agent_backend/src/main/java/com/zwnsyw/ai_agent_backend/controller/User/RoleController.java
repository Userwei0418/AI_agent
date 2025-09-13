package com.zwnsyw.ai_agent_backend.controller.User;

import com.zwnsyw.ai_agent_backend.RBAC.annotation.PreAuthorizeRole;
import com.zwnsyw.ai_agent_backend.common.response.BaseResponse;
import com.zwnsyw.ai_agent_backend.common.response.ResultUtils;
import com.zwnsyw.ai_agent_backend.entity.User.Role;
import com.zwnsyw.ai_agent_backend.service.User.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/roles")
public class RoleController {
    @Autowired
    private RoleService roleService;

    @GetMapping
    @PreAuthorizeRole("ADMIN")
    public BaseResponse<List<Role>> getAllRoles() {
        return ResultUtils.success(roleService.list());
    }

    @PostMapping
    @PreAuthorizeRole("ADMIN")
    public BaseResponse<Role> createRole(@RequestBody Role role) {
        roleService.save(role);
        return ResultUtils.success(role);
    }

    @PutMapping("/{roleId}")
    @PreAuthorizeRole("ADMIN")
    public BaseResponse<Role> updateRole(@PathVariable Long roleId, @RequestBody Role role) {
        role.setId(roleId);
        roleService.updateById(role);
        return ResultUtils.success(role);
    }

    @DeleteMapping("/{roleId}")
    @PreAuthorizeRole("ADMIN")
    public BaseResponse<Boolean> deleteRole(@PathVariable Long roleId) {
        roleService.removeById(roleId);
        return ResultUtils.success(true);
    }

}