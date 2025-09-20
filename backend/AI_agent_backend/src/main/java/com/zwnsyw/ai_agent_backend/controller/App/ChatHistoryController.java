package com.zwnsyw.ai_agent_backend.controller.App;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zwnsyw.ai_agent_backend.RBAC.annotation.Anonymous;
import com.zwnsyw.ai_agent_backend.RBAC.annotation.PreAuthorizeRole;
import com.zwnsyw.ai_agent_backend.common.response.BaseResponse;
import com.zwnsyw.ai_agent_backend.common.response.ResultUtils;
import com.zwnsyw.ai_agent_backend.dto.App.ChatHistoryQueryRequest;
import com.zwnsyw.ai_agent_backend.entity.App.ChatHistory;
import com.zwnsyw.ai_agent_backend.exception.ErrorCode;
import com.zwnsyw.ai_agent_backend.exception.ThrowUtils;
import com.zwnsyw.ai_agent_backend.service.App.ChatHistoryService;
import com.zwnsyw.ai_agent_backend.service.User.UserService;
import com.zwnsyw.ai_agent_backend.vo.User.UserVO;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;


@RestController
@RequestMapping("/chatHistory")
public class ChatHistoryController {

    @Resource
    private UserService userService;

    @Resource
    private ChatHistoryService chatHistoryService;


    /**
     * 分页查询某个应用的对话历史（游标查询）
     *
     * @param appId          应用ID
     * @param pageSize       页面大小
     * @param lastCreateTime 最后一条记录的创建时间
     * @param request        请求
     * @return 对话历史分页
     */
    @GetMapping("/app/{appId}")
    @Anonymous
    public BaseResponse<Page<ChatHistory>> listAppChatHistory(@PathVariable Long appId,
                                                              @RequestParam(defaultValue = "10") int pageSize,
                                                              @RequestParam(required = false) LocalDateTime lastCreateTime,
                                                              HttpServletRequest request) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID不能为空");
        ThrowUtils.throwIf(pageSize <= 0 || pageSize > 50, ErrorCode.PARAMS_ERROR, "页面大小必须在1-50之间");

        UserVO loginUser = userService.getLoginUser(request);

        Page<ChatHistory> result = chatHistoryService.listAppChatHistoryByPage(appId, pageSize, lastCreateTime, loginUser);

        return ResultUtils.success(result);
    }

    /**
     * 管理员分页查询所有对话历史
     *
     * @param chatHistoryQueryRequest 查询请求
     * @return 对话历史分页
     */
    @PostMapping("/admin/list")
    @PreAuthorizeRole("ADMIN")
    public BaseResponse<Page<ChatHistory>> listAllChatHistoryByPageForAdmin(@RequestBody ChatHistoryQueryRequest chatHistoryQueryRequest) {
        ThrowUtils.throwIf(chatHistoryQueryRequest == null, ErrorCode.PARAMS_ERROR, "参数不能为空");
        long pageNum = chatHistoryQueryRequest.getPage();
        long pageSize = chatHistoryQueryRequest.getPageSize();

        // 查询数据
        QueryWrapper<ChatHistory> queryWrapper = chatHistoryService.getQueryWrapper(chatHistoryQueryRequest);
        Page<ChatHistory> result = chatHistoryService.page(Page.of(pageNum, pageSize), queryWrapper);

        return ResultUtils.success(result);
    }

}
