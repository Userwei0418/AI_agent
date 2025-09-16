package com.zwnsyw.ai_agent_backend.service.serviceimpl.App;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zwnsyw.ai_agent_backend.dto.App.AppCreateRequest;
import com.zwnsyw.ai_agent_backend.dto.App.AppQueryRequest;
import com.zwnsyw.ai_agent_backend.dto.App.AppUpdateRequest;
import com.zwnsyw.ai_agent_backend.entity.App.App;
import com.zwnsyw.ai_agent_backend.entity.User.User;
import com.zwnsyw.ai_agent_backend.exception.ErrorCode;
import com.zwnsyw.ai_agent_backend.exception.ThrowUtils;
import com.zwnsyw.ai_agent_backend.mapper.App.AppMapper;
import com.zwnsyw.ai_agent_backend.service.App.AppService;
import com.zwnsyw.ai_agent_backend.service.User.UserService;
import com.zwnsyw.ai_agent_backend.vo.App.AppVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.zwnsyw.ai_agent_backend.contant.AppConstant.GOOD_APP_PRIORITY;
import static com.zwnsyw.ai_agent_backend.contant.AppConstant.DEFAULT_APP_PRIORITY;

@Service
@Slf4j
public class AppServiceImpl extends ServiceImpl<AppMapper, App> implements AppService {

    @Resource
    private UserService userService;

    @Override
    public long createApp(AppCreateRequest appCreateRequest, Long userId) {

        // 创建应用
        App app = new App();
        String initPrompt = appCreateRequest.getInitPrompt();
        // 应用名称暂时为 initPrompt 前 12 位
        app.setAppName(initPrompt.substring(0, Math.min(initPrompt.length(), 12)));
        app.setInitPrompt(appCreateRequest.getInitPrompt());
        app.setCodeGenType(appCreateRequest.getCodeGenType());
        app.setUserId(userId);
        app.setCreateTime(new Date());
        app.setUpdateTime(new Date());

        boolean result = this.save(app);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "创建应用失败");
        log.info("应用创建成功，ID: {}", app.getId());
        return app.getId();
    }

    @Override
    public AppVO updateMyApp(AppUpdateRequest appUpdateRequest, Long userId) {

        // 检查应用是否存在且属于当前用户
        App app = this.getById(appUpdateRequest.getId());
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        ThrowUtils.throwIf(!app.getUserId().equals(userId), ErrorCode.FORBIDDEN_ERROR, "无权限操作该应用");

        // 更新应用名称
        if (appUpdateRequest.getAppName() != null && !appUpdateRequest.getAppName().equals(app.getAppName())) {
            app.setAppName(appUpdateRequest.getAppName());
            app.setUpdateTime(new Date());
            boolean result = this.updateById(app);
            ThrowUtils.throwIf(!result, ErrorCode.SYSTEM_ERROR, "更新应用失败");
        }

        return convertToAppVO(app);
    }

    @Override
    public boolean deleteMyApp(Long appId, Long userId) {

        // 检查应用是否存在且属于当前用户
        App app = this.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        ThrowUtils.throwIf(!app.getUserId().equals(userId), ErrorCode.FORBIDDEN_ERROR, "无权限操作该应用");

        // 逻辑删除应用
        LambdaUpdateWrapper<App> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(App::getId, appId)
                .set(App::getIsDelete, 1);
        return this.update(updateWrapper);
    }

    @Override
    public AppVO getAppById(Long appId) {

        App app = this.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        ThrowUtils.throwIf(app.getIsDelete() == 1, ErrorCode.NOT_FOUND_ERROR, "应用不存在");

        return convertToAppVO(app);
    }

    @Override
    public Page<AppVO> getMyApps(AppQueryRequest queryRequest, Long userId) {
        QueryWrapper<App> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId);
        queryWrapper.eq("isDelete", 0);

        // 添加搜索条件
        if (queryRequest.getAppName() != null && !queryRequest.getAppName().isEmpty()) {
            queryWrapper.like("appName", queryRequest.getAppName());
        }
        if (queryRequest.getKeyword() != null && !queryRequest.getKeyword().isEmpty()) {
            queryWrapper.like("appName", queryRequest.getKeyword());
        }

        // 分页查询
        Page<App> page = new Page<>(queryRequest.getPage(), Math.min(queryRequest.getPageSize(), 20));
        Page<App> appPage = this.page(page, queryWrapper);

        // 转换为VO
        Page<AppVO> appVOPage = new Page<>(appPage.getCurrent(), appPage.getSize(), appPage.getTotal());
        appVOPage.setRecords(getAppVOList(appPage.getRecords()));

        return appVOPage;
    }


    @Override
    public Page<AppVO> getFeaturedApps(AppQueryRequest queryRequest) {
        QueryWrapper<App> queryWrapper = new QueryWrapper<>();
        queryWrapper.ge("priority", GOOD_APP_PRIORITY);
        queryWrapper.eq("isDelete", 0);

        // 添加搜索条件
        if (queryRequest.getAppName() != null && !queryRequest.getAppName().isEmpty()) {
            queryWrapper.like("appName", queryRequest.getAppName());
        }
        if (queryRequest.getKeyword() != null && !queryRequest.getKeyword().isEmpty()) {
            queryWrapper.like("appName", queryRequest.getKeyword());
        }

        // 按优先级排序
        queryWrapper.orderByDesc("priority");

        // 分页查询
        Page<App> page = new Page<>(queryRequest.getPage(), Math.min(queryRequest.getPageSize(), 20));
        Page<App> appPage = this.page(page, queryWrapper);

        // 转换为VO
        Page<AppVO> appVOPage = new Page<>(appPage.getCurrent(), appPage.getSize(), appPage.getTotal());
        appVOPage.setRecords(getAppVOList(appPage.getRecords()));

        return appVOPage;
    }


    @Override
    public boolean deleteAppByAdmin(Long appId) {

        App app = this.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");

        // 逻辑删除应用
        LambdaUpdateWrapper<App> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(App::getId, appId)
                .set(App::getIsDelete, 1);
        return this.update(updateWrapper);
    }

    @Override
    public AppVO updateAppByAdmin(AppUpdateRequest appUpdateRequest) {

        // 检查应用是否存在
        App app = this.getById(appUpdateRequest.getId());
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");

        // 更新应用信息
        app.setAppName(appUpdateRequest.getAppName());
        app.setCover(appUpdateRequest.getCover());
        app.setPriority(appUpdateRequest.getPriority());
        app.setUpdateTime(new Date());

        boolean result = this.updateById(app);
        ThrowUtils.throwIf(!result, ErrorCode.SYSTEM_ERROR, "更新应用失败");

        return convertToAppVO(app);
    }

    @Override
    public Page<AppVO> getAllApps(AppQueryRequest queryRequest) {
        ThrowUtils.throwIf(queryRequest == null, ErrorCode.PARAMS_ERROR);

        QueryWrapper<App> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("isDelete", 0);

        // 添加搜索条件
        if (queryRequest.getId() != null) {
            queryWrapper.eq("id", queryRequest.getId());
        }
        if (queryRequest.getAppName() != null && !queryRequest.getAppName().isEmpty()) {
            queryWrapper.like("appName", queryRequest.getAppName());
        }
        if (queryRequest.getCodeGenType() != null && !queryRequest.getCodeGenType().isEmpty()) {
            queryWrapper.eq("codeGenType", queryRequest.getCodeGenType());
        }
        if (queryRequest.getUserId() != null) {
            queryWrapper.eq("userId", queryRequest.getUserId());
        }
        if (queryRequest.getKeyword() != null && !queryRequest.getKeyword().isEmpty()) {
            queryWrapper.and(wrapper -> wrapper
                    .like("appName", queryRequest.getKeyword())
                    .or()
                    .like("codeGenType", queryRequest.getKeyword()));
        }

        // 分页查询
        Page<App> page = new Page<>(queryRequest.getPage(), queryRequest.getPageSize());
        Page<App> appPage = this.page(page, queryWrapper);

        // 转换为VO
        Page<AppVO> appVOPage = new Page<>(appPage.getCurrent(), appPage.getSize(), appPage.getTotal());
        appVOPage.setRecords(getAppVOList(appPage.getRecords()));

        return appVOPage;
    }



    @Override
    public AppVO convertToAppVO(App app) {
        if (app == null) {
            return null;
        }

        AppVO appVO = new AppVO();
        appVO.setId(app.getId());
        appVO.setAppName(app.getAppName());
        appVO.setCover(app.getCover());
        appVO.setInitPrompt(app.getInitPrompt());
        appVO.setCodeGenType(app.getCodeGenType());
        appVO.setDeployKey(app.getDeployKey());

        if (app.getDeployedTime() != null) {
            appVO.setDeployedTime(app.getDeployedTime().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime());
        }

        appVO.setPriority(app.getPriority());
        appVO.setUserId(app.getUserId());

        // 类型转换：Date -> LocalDateTime
        if (app.getCreateTime() != null) {
            appVO.setCreateTime(app.getCreateTime().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime());
        }

        if (app.getUpdateTime() != null) {
            appVO.setUpdateTime(app.getUpdateTime().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime());
        }

        // 添加用户信息
        if (app.getUserId() != null) {
            User user = userService.getById(app.getUserId());
            if (user != null) {
                appVO.setUser(userService.convertToLoginUserVO(user));
            }
        }

        return appVO;
    }

    @Override
    public List<AppVO> getAppVOList(List<App> appList) {
        if (appList == null || appList.isEmpty()) {
            return new ArrayList<>();
        }

        // 批量获取用户信息，避免 N+1 查询问题
        Set<Long> userIds = appList.stream()
                .map(App::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, User> userMap;
        if (!userIds.isEmpty()) {
            List<User> users = userService.listByIds(new ArrayList<>(userIds));
            userMap = users.stream()
                    .collect(Collectors.toMap(User::getId, user -> user));
        } else {
            userMap = new HashMap<>();
        }

        return appList.stream().map(app -> {
            AppVO appVO = convertToAppVO(app);
            Long userId = app.getUserId();
            if (userId != null && userMap.containsKey(userId)) {
                User user = userMap.get(userId);
                appVO.setUser(userService.convertToLoginUserVO(user));
            }
            return appVO;
        }).collect(Collectors.toList());
    }

}