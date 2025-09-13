package com.zwnsyw.ai_agent_backend.controller.User;

import com.zwnsyw.ai_agent_backend.RBAC.annotation.PreAuthorizeRole;
import com.zwnsyw.ai_agent_backend.common.response.BaseResponse;
import com.zwnsyw.ai_agent_backend.common.response.ResultUtils;
import com.zwnsyw.ai_agent_backend.entity.User.UserRole;
import com.zwnsyw.ai_agent_backend.service.User.UserRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user-roles")
public class UserRoleController {
    @Autowired
    private UserRoleService userRoleService;

    @GetMapping
    @PreAuthorizeRole("ADMIN")
    public BaseResponse<List<UserRole>> getAllUserRoles() {
        return ResultUtils.success(userRoleService.list());
    }

    @PostMapping
    @PreAuthorizeRole("ADMIN")
    public BaseResponse<UserRole> createUserRole(@RequestBody UserRole userRole) {
        userRoleService.save(userRole);
        return ResultUtils.success(userRole);
    }

    @DeleteMapping("/{userId}/{roleId}")
    @PreAuthorizeRole("ADMIN")
    public BaseResponse<Boolean> deleteUserRole(@PathVariable Long userId, @PathVariable Long roleId) {
        userRoleService.removeById(new UserRole(userId, roleId));
        return ResultUtils.success(true);
    }

}