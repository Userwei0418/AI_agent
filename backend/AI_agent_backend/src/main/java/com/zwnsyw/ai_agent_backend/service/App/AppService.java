// AppService.java
package com.zwnsyw.ai_agent_backend.service.App;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zwnsyw.ai_agent_backend.dto.App.AppCreateRequest;
import com.zwnsyw.ai_agent_backend.dto.App.AppQueryRequest;
import com.zwnsyw.ai_agent_backend.dto.App.AppUpdateRequest;
import com.zwnsyw.ai_agent_backend.entity.App.App;
import com.zwnsyw.ai_agent_backend.vo.App.AppVO;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface AppService extends IService<App> {
    /**
     * 创建应用
     *
     * @param appCreateRequest 应用创建请求
     * @param userId           用户ID
     * @return 应用ID
     */
    long createApp(AppCreateRequest appCreateRequest, Long userId);

    /**
     * 更新自己的应用（目前只支持修改应用名称）
     *
     * @param appUpdateRequest 应用更新请求
     * @param userId           用户ID
     * @return 更新后的应用VO
     */
    AppVO updateMyApp(AppUpdateRequest appUpdateRequest, Long userId);

    /**
     * 删除自己的应用
     *
     * @param appId  应用ID
     * @param userId 用户ID
     * @return 删除结果
     */
    boolean deleteMyApp(Long appId, Long userId);

    /**
     * 查看应用详情
     *
     * @param appId 应用ID
     * @return 应用VO
     */
    AppVO getAppById(Long appId);

    /**
     * 分页查询自己的应用列表（支持根据名称查询，每页最多 20 个）
     *
     * @param queryRequest 查询请求
     * @param userId       用户ID
     * @return 应用VO分页
     */
    Page<AppVO> getMyApps(AppQueryRequest queryRequest, Long userId);

    /**
     * 分页查询精选的应用列表（支持根据名称查询，每页最多 20 个）
     *
     * @param queryRequest 查询请求
     * @return 应用VO分页
     */
    Page<AppVO> getFeaturedApps(AppQueryRequest queryRequest);

    /**
     * 管理员删除任意应用
     *
     * @param appId 应用ID
     * @return 删除结果
     */
    boolean deleteAppByAdmin(Long appId);

    /**
     * 管理员更新任意应用（支持更新应用名称、应用封面、优先级）
     *
     * @param appUpdateRequest 应用更新请求
     * @return 更新后的应用VO
     */
    AppVO updateAppByAdmin(AppUpdateRequest appUpdateRequest);

    /**
     * 管理员分页查询应用列表（支持根据除时间外的任何字段查询，每页数量不限）
     *
     * @param queryRequest 查询请求
     * @return 应用VO分页
     */
    Page<AppVO> getAllApps(AppQueryRequest queryRequest);

    /**
     * 将App实体转换为AppVO
     *
     * @param app App实体
     * @return AppVO
     */
    AppVO convertToAppVO(App app);

    /**
     * 批量获取应用VO列表
     *
     * @param appList 应用列表
     * @return 应用VO列表
     */
    List<AppVO> getAppVOList(List<App> appList);

}
