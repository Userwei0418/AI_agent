package com.zwnsyw.ai_agent_backend.controller.App;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zwnsyw.ai_agent_backend.RBAC.annotation.Anonymous;
import com.zwnsyw.ai_agent_backend.RBAC.annotation.PreAuthorize;
import com.zwnsyw.ai_agent_backend.RBAC.annotation.PreAuthorizeRole;
import com.zwnsyw.ai_agent_backend.common.request.DeleteRequest;
import com.zwnsyw.ai_agent_backend.common.response.BaseResponse;
import com.zwnsyw.ai_agent_backend.common.response.ResultUtils;
import com.zwnsyw.ai_agent_backend.dto.App.AppCreateRequest;
import com.zwnsyw.ai_agent_backend.dto.App.AppQueryRequest;
import com.zwnsyw.ai_agent_backend.dto.App.AppUpdateRequest;
import com.zwnsyw.ai_agent_backend.entity.App.App;
import com.zwnsyw.ai_agent_backend.entity.User.User;
import com.zwnsyw.ai_agent_backend.exception.ErrorCode;
import com.zwnsyw.ai_agent_backend.exception.ThrowUtils;
import com.zwnsyw.ai_agent_backend.service.App.AppService;
import com.zwnsyw.ai_agent_backend.service.User.UserService;
import com.zwnsyw.ai_agent_backend.vo.App.AppVO;
import com.zwnsyw.ai_agent_backend.vo.User.UserVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/app")
@Validated
public class AppController {

    @Autowired
    private AppService appService;

    @Autowired
    private UserService userService;

    /**
     * 创建应用
     */
    @PostMapping("/create")
    @PreAuthorize("system:app:add")
    public BaseResponse<Long> createApp(@RequestBody @Valid AppCreateRequest appCreateRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(appCreateRequest == null, ErrorCode.PARAMS_ERROR, "请求参数不能为空");

        UserVO loginUser = userService.getLoginUser(request);
        long appId = appService.createApp(appCreateRequest, loginUser.getId());

        return ResultUtils.success(appId);
    }

    /**
     * 根据 id 修改自己的应用（目前只支持修改应用名称）
     */
    @PostMapping("/update")
    @PreAuthorize("system:app:edit")
    public BaseResponse<AppVO> updateMyApp(@RequestBody @Valid AppUpdateRequest appUpdateRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(appUpdateRequest == null, ErrorCode.PARAMS_ERROR, "请求参数不能为空");

        UserVO loginUser = userService.getLoginUser(request);
        AppVO appVO = appService.updateMyApp(appUpdateRequest, loginUser.getId());

        return ResultUtils.success(appVO);
    }

    /**
     * 根据 id 删除自己的应用
     */
    @DeleteMapping("/delete")
    @PreAuthorize("system:app:remove")
    public BaseResponse<Boolean> deleteMyApp(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() <= 0, ErrorCode.PARAMS_ERROR, "应用ID不能为空");

        long appId = deleteRequest.getId();

        UserVO loginUser = userService.getLoginUser(request);
        boolean result = appService.deleteMyApp(appId, loginUser.getId());

        return ResultUtils.success(result);
    }

    /**
     * 根据 id 查看应用详情
     */
    @GetMapping("/get/{appId}")
    @Anonymous
    public BaseResponse<AppVO> getAppById(@PathVariable Long appId) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID不能为空");

        AppVO appVO = appService.getAppById(appId);

        return ResultUtils.success(appVO);
    }

    /**
     * 分页查询自己的应用列表（支持根据名称查询，每页最多 20 个）
     */
    @GetMapping("/my/list")
    @PreAuthorize("system:app:query")
    public BaseResponse<Page<AppVO>> getMyApps(AppQueryRequest queryRequest, HttpServletRequest request) {

        ThrowUtils.throwIf(queryRequest == null, ErrorCode.PARAMS_ERROR, "请求参数不能为空");

        UserVO loginUser = userService.getLoginUser(request);
        Page<AppVO> appVOPage = appService.getMyApps(queryRequest, loginUser.getId());

        return ResultUtils.success(appVOPage);
    }


    /**
     * 分页查询精选的应用列表（支持根据名称查询，每页最多 20 个）
     */
    @GetMapping("/featured/list")
    @Anonymous
    public BaseResponse<Page<AppVO>> getFeaturedApps(AppQueryRequest queryRequest) {
        ThrowUtils.throwIf(queryRequest == null, ErrorCode.PARAMS_ERROR, "请求参数不能为空");

        Page<AppVO> appVOPage = appService.getFeaturedApps(queryRequest);

        return ResultUtils.success(appVOPage);
    }

    /**
     * 【管理员】根据 id 删除任意应用
     */
    @DeleteMapping("/admin/delete")
    @PreAuthorizeRole("ADMIN")
    public BaseResponse<Boolean> deleteAppByAdmin(@RequestBody @Valid DeleteRequest deleteRequest) {

        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() <= 0, ErrorCode.PARAMS_ERROR, "应用ID不能为空");

        boolean result = appService.deleteAppByAdmin(deleteRequest.getId());

        return ResultUtils.success(result);
    }

    /**
     * 【管理员】根据 id 更新任意应用（支持更新应用名称、应用封面、优先级）
     */
    @PostMapping("/admin/update")
    @PreAuthorizeRole("ADMIN")
    public BaseResponse<AppVO> updateAppByAdmin(@RequestBody @Valid AppUpdateRequest appUpdateRequest) {

        ThrowUtils.throwIf(appUpdateRequest == null, ErrorCode.PARAMS_ERROR, "请求参数不能为空");

        AppVO appVO = appService.updateAppByAdmin(appUpdateRequest);

        return ResultUtils.success(appVO);
    }

    /**
     * 【管理员】分页查询应用列表（支持根据除时间外的任何字段查询，每页数量不限）
     */
    @GetMapping("/admin/list")
    @PreAuthorizeRole("ADMIN")
    public BaseResponse<Page<AppVO>> getAllApps(AppQueryRequest queryRequest) {
        Page<AppVO> appVOPage = appService.getAllApps(queryRequest);

        return ResultUtils.success(appVOPage);
    }
}
