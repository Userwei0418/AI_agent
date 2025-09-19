package com.zwnsyw.ai_agent_backend.service.serviceimpl.App;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zwnsyw.ai_agent_backend.contant.AppConstant;
import com.zwnsyw.ai_agent_backend.core.AiCodeGeneratorFacade;
import com.zwnsyw.ai_agent_backend.dto.App.AppCreateRequest;
import com.zwnsyw.ai_agent_backend.dto.App.AppQueryRequest;
import com.zwnsyw.ai_agent_backend.dto.App.AppUpdateRequest;
import com.zwnsyw.ai_agent_backend.entity.App.App;
import com.zwnsyw.ai_agent_backend.entity.User.User;
import com.zwnsyw.ai_agent_backend.enums.AiEnums.CodeGenTypeEnum;
import com.zwnsyw.ai_agent_backend.exception.BusinessException;
import com.zwnsyw.ai_agent_backend.exception.ErrorCode;
import com.zwnsyw.ai_agent_backend.exception.ThrowUtils;
import com.zwnsyw.ai_agent_backend.mapper.App.AppMapper;
import com.zwnsyw.ai_agent_backend.service.App.AppService;
import com.zwnsyw.ai_agent_backend.service.User.UserService;
import com.zwnsyw.ai_agent_backend.vo.App.AppVO;
import com.zwnsyw.ai_agent_backend.vo.User.UserVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.LocalDateTime;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import static com.zwnsyw.ai_agent_backend.contant.AppConstant.GOOD_APP_PRIORITY;
import static com.zwnsyw.ai_agent_backend.contant.AppConstant.DEFAULT_APP_PRIORITY;

@Service
@Slf4j
public class AppServiceImpl extends ServiceImpl<AppMapper, App> implements AppService {

    @Resource
    private UserService userService;

    @Resource
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;

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
        return convertToAppVO(app, null);
    }

    public AppVO convertToAppVO(App app, Map<Long, User> userMap) {
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

        // 添加用户信息（如果提供了userMap，则从其中获取用户信息）
        if (app.getUserId() != null && userMap != null && userMap.containsKey(app.getUserId())) {
            User user = userMap.get(app.getUserId());
            if (user != null) {
                appVO.setUser(userService.convertToLoginUserVO(user));
            }
        } else if (app.getUserId() != null && userMap == null) {
            // 向后兼容，如果没有提供userMap，则单独查询用户信息
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
            AppVO appVO = convertToAppVO(app, userMap);
            Long userId = app.getUserId();
            if (userId != null && userMap.containsKey(userId)) {
                User user = userMap.get(userId);
                appVO.setUser(userService.convertToLoginUserVO(user));
            }
            return appVO;
        }).collect(Collectors.toList());
    }



    @Override
    public Flux<String> chatToGenCode(Long appId, String message, UserVO loginUser) {
        try {
            // 查询应用信息
            App app = this.getById(appId);
            ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");

            // 验证用户是否有权限访问该应用，仅本人可以生成代码
            ThrowUtils.throwIf(!app.getUserId().equals(loginUser.getId()), ErrorCode.NO_AUTH_ERROR, "无权限访问该应用");

            // 获取应用的代码生成类型
            String codeGenTypeStr = app.getCodeGenType();
            CodeGenTypeEnum codeGenTypeEnum = CodeGenTypeEnum.getEnumByValue(codeGenTypeStr);
            ThrowUtils.throwIf(codeGenTypeEnum == null, ErrorCode.SYSTEM_ERROR, "不支持的代码生成类型");

            // 调用 AI 生成代码
            return aiCodeGeneratorFacade.generateAndSaveCodeStream(message, codeGenTypeEnum, appId)
                    .onErrorResume(throwable -> {
                        log.error("AI代码生成失败，appId: {}, message: {}", appId, message, throwable);
                        return Flux.just("系统内部异常: " + throwable.getMessage());
                    });
        } catch (Exception e) {
            log.error("chatToGenCode方法执行异常，appId: {}, message: {}", appId, message, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "代码生成失败: " + e.getMessage());
        }
    }


    @Override
    public String deployApp(Long appId, UserVO loginUser) {

        // 查询应用信息
        App app = this.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        // 验证用户是否有权限部署该应用，仅本人可以部署
        if (!app.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限部署该应用");
        }

        // 检查是否已有 deployKey
        String deployKey = app.getDeployKey();
        // 没有则生成 6 位 deployKey（大小写字母 + 数字）
        if (StrUtil.isBlank(deployKey)) {
            deployKey = RandomUtil.randomString(6);
        }

        // 获取代码生成类型，构建源目录路径
        String codeGenType = app.getCodeGenType();
        String sourceDirName = codeGenType + "_" + appId;
        String sourceDirPath = AppConstant.CODE_OUTPUT_ROOT_DIR + File.separator + sourceDirName;

        // 检查源目录是否存在
        File sourceDir = new File(sourceDirPath);
        if (!sourceDir.exists() || !sourceDir.isDirectory()) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "应用代码不存在，请先生成代码");
        }

        // 复制文件到部署目录
        String deployDirPath = AppConstant.CODE_DEPLOY_ROOT_DIR + File.separator + deployKey;
        try {
            FileUtil.copyContent(sourceDir, new File(deployDirPath), true);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "部署失败：" + e.getMessage());
        }

        // 更新应用的 deployKey 和部署时间
        App updateApp = new App();
        updateApp.setId(appId);
        updateApp.setDeployKey(deployKey);
        updateApp.setDeployedTime(LocalDateTime.now().toDate());
        boolean updateResult = this.updateById(updateApp);
        ThrowUtils.throwIf(!updateResult, ErrorCode.OPERATION_ERROR, "更新应用部署信息失败");

        // 返回可访问的 URL
        return String.format("%s/%s/", AppConstant.CODE_DEPLOY_HOST, deployKey);
    }


}