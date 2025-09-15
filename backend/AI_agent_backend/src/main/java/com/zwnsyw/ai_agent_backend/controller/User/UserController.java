package com.zwnsyw.ai_agent_backend.controller.User;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zwnsyw.ai_agent_backend.RBAC.annotation.Anonymous;
import com.zwnsyw.ai_agent_backend.RBAC.annotation.PreAuthorize;
import com.zwnsyw.ai_agent_backend.RBAC.annotation.PreAuthorizeRole;
import com.zwnsyw.ai_agent_backend.common.response.BaseResponse;
import com.zwnsyw.ai_agent_backend.common.response.ResultUtils;
import com.zwnsyw.ai_agent_backend.dto.User.*;
import com.zwnsyw.ai_agent_backend.dto.utils.UpdateGroup;
import com.zwnsyw.ai_agent_backend.enums.UserEnums.UserStatusEnum;
import com.zwnsyw.ai_agent_backend.exception.ErrorCode;
import com.zwnsyw.ai_agent_backend.exception.ThrowUtils;
import com.zwnsyw.ai_agent_backend.service.User.UserService;

import com.zwnsyw.ai_agent_backend.vo.User.UserVO;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;



import static com.zwnsyw.ai_agent_backend.contant.UserConstant.ADMIN_ROLE;
import static com.zwnsyw.ai_agent_backend.contant.UserConstant.USER_LOGIN_STATE;

@RestController
@RequestMapping("/user")
@Validated
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping("/register")
    @Anonymous
    public BaseResponse<Long> userRegister(@RequestBody @Valid UserRegisterRequest userRegisterRequest) {
        ThrowUtils.throwIf(userRegisterRequest == null, ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        long result = userService.userRegister(
                userRegisterRequest.getUserAccount(),
                userRegisterRequest.getUserPassword(),
                userRegisterRequest.getCheckPassword(),
                userRegisterRequest.getInviteCode()
        );
        return ResultUtils.success(result);
    }

    @PostMapping("/login")
    @Anonymous
    public BaseResponse<UserVO> userLoginBySession(
            @RequestBody @Valid UserLoginRequest userLoginRequest,
            HttpServletRequest request,
            HttpServletResponse response) {

        ThrowUtils.throwIf(userLoginRequest == null, ErrorCode.PARAMS_ERROR, "请求参数不能为空");

        UserVO loginUserVO = userService.userLoginBySession(userLoginRequest.getUserAccount(),
                userLoginRequest.getUserPassword(),
                request);

        ThrowUtils.throwIf(loginUserVO == null, ErrorCode.PARAMS_ERROR, "登录失败，用户信息为空");

        // 配置 HttpOnly Cookie，用于存储 `SessionID`
        // 当用户再次访问服务器时，浏览器会自动将此 Cookie 发送到服务器
        // 服务器可以通过该 Cookie 中的 SESSIONID 来识别用户的会话信息，从而保持用户的登录状态
        Cookie sessionCookie = new Cookie("SESSIONID", request.getSession().getId());
        sessionCookie.setHttpOnly(true); // 防止前端 JS 获取，提升安全性
        sessionCookie.setPath("/");      // 适用于整个域
        sessionCookie.setMaxAge(7 * 24 * 60 * 60); // 过期时间 7 天
        sessionCookie.setSecure(true); // 仅在 HTTPS 环境生效(暂未配置证书 不开启)
        response.addCookie(sessionCookie);

        return ResultUtils.success(loginUserVO);
    }


    @GetMapping("/current")
    @Anonymous
    public BaseResponse<UserVO> CurrentUser(HttpServletRequest request) {

        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR, "请求不能为空");

        return ResultUtils.success(userService.getLoginUser(request));
    }

    @PostMapping("/logout")
    @Anonymous
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR, "请求不能为空");
        boolean result = userService.userLogout(request);
        return ResultUtils.success(result);
    }

    @GetMapping("/search")
    @PreAuthorize("system:user:query")
    public BaseResponse<Page<UserVO>> searchUsers(@ModelAttribute @Valid UserQueryRequest queryRequest) {
        ThrowUtils.throwIf(queryRequest == null, ErrorCode.PARAMS_ERROR, "请求不能为空");
        Page<UserVO> users = userService.getUsersByKeyword(queryRequest);
        return ResultUtils.success(users);
    }

    @GetMapping("/all")
    @PreAuthorizeRole("ADMIN")
    public BaseResponse<Page<UserVO>> getAllUsers(HttpServletRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR, "请求不能为空");
        Page<UserVO> users = userService.getAllUsers(request);
        return ResultUtils.success(users);
    }

    @PutMapping("/status/{userId}")
    @PreAuthorizeRole("ADMIN")
    public BaseResponse<Boolean> updateUserStatus(@PathVariable Long userId, @RequestParam String status) {
        UserStatusEnum userStatusEnum = UserStatusEnum.fromCode(status);
        return ResultUtils.success(userService.updateUserStatus(userId, userStatusEnum));
    }

    @DeleteMapping("/delete/{userId}")
    @PreAuthorizeRole("ADMIN")
    public BaseResponse<Boolean> deleteUser(@PathVariable Long userId) {
        ThrowUtils.throwIf(userId == null || userId <= 0, ErrorCode.PARAMS_ERROR, "用户ID不能为空或无效");
        boolean result = userService.deleteUser(userId);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "用户删除失败");
        return ResultUtils.success(true);
    }


    @PutMapping("/reset-password/{userId}")
    @PreAuthorizeRole("ADMIN")
    public BaseResponse<Boolean> resetUserPassword(@PathVariable Long userId) {
        String defaultPassword = "ai_agent";
        boolean result = userService.resetUserPassword(userId, defaultPassword);
        return ResultUtils.success(result);
    }


    @PostMapping("/update")
    @PreAuthorize("system:user:edit")
    public BaseResponse<UserVO> updateUser(@RequestBody @Validated(UpdateGroup.class) UserUpdateRequest userUpdateRequest, HttpServletRequest request) {

        ThrowUtils.throwIf(userUpdateRequest == null, ErrorCode.PARAMS_ERROR, "用户更新信息不能为空");

        UserVO loginUser = userService.getLoginUser(request);
        boolean isAdmin = loginUser.getRoles().contains(ADMIN_ROLE);
        ThrowUtils.throwIf(!isAdmin && !loginUser.getId().equals(userUpdateRequest.getId()), ErrorCode.FORBIDDEN_ERROR, "无权修改其他用户信息");

        UserVO updatedUserVO = userService.updateUser(userUpdateRequest);

        // 更新成功后，重新设置 session 中的用户信息，确保 session 中保存的是最新的用户数据
        request.getSession().setAttribute(USER_LOGIN_STATE, updatedUserVO);

        return ResultUtils.success(updatedUserVO);
    }

    @PostMapping("/uploadAvatar")
    @PreAuthorizeRole("USER")
    public BaseResponse<UserVO> uploadAvatar(
            @RequestPart("file") MultipartFile multipartFile,
            HttpServletRequest request) {

        UserVO loginUser = userService.getLoginUser(request);

        UserVO updatedUser = userService.uploadAvatar(multipartFile, loginUser);

        request.getSession().setAttribute(USER_LOGIN_STATE, updatedUser);

        return ResultUtils.success(updatedUser);
    }

    @PostMapping("/update-password")
    @PreAuthorize("system:user:edit")
    public BaseResponse<Boolean> updatePassword(@RequestBody @Valid UserPasswordUpdateRequest userPasswordUpdateRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(userPasswordUpdateRequest == null, ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        boolean result = userService.updatePassword(
                userPasswordUpdateRequest.getOldPassword(),
                userPasswordUpdateRequest.getNewPassword(),
                request
        );
        return ResultUtils.success(result);
    }


    //todo debug 只能单向设置 不能取消权限
    @PostMapping("/set-role")
    @PreAuthorizeRole("ADMIN")
    public BaseResponse<Boolean> setRole(@RequestBody @Valid UserRoleRequest userRoleRequest) {
        ThrowUtils.throwIf(userRoleRequest == null, ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        return ResultUtils.success(userService.assignRole(userRoleRequest.getUserId(), userRoleRequest.getRoleName()));
    }
}