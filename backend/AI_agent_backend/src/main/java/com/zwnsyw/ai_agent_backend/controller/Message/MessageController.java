package com.zwnsyw.ai_agent_backend.controller.Message;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zwnsyw.ai_agent_backend.RBAC.annotation.PreAuthorizeRole;
import com.zwnsyw.ai_agent_backend.common.response.BaseResponse;
import com.zwnsyw.ai_agent_backend.common.response.ResultUtils;
import com.zwnsyw.ai_agent_backend.entity.Message.Message;
import com.zwnsyw.ai_agent_backend.enums.MessageEnums.MessageTypeEnum;
import com.zwnsyw.ai_agent_backend.service.Message.MessageService;
import com.zwnsyw.ai_agent_backend.service.User.UserService;
import com.zwnsyw.ai_agent_backend.vo.User.UserVO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


import java.util.List;


@RestController
@RequestMapping("/messages")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private UserService userService;

    @GetMapping
    @PreAuthorizeRole("USER")
    public BaseResponse<List<Message>> getMessages(HttpServletRequest request) {

        UserVO loginUser = userService.getLoginUser(request);
        Long userId = loginUser.getId();

        List<Message> messageList = messageService.getMessages(userId);
        return ResultUtils.success(messageList);
    }

    @PutMapping("/{messageId}/read")
    @PreAuthorizeRole("USER")
    public BaseResponse<Boolean> markAsRead(@PathVariable Long messageId, HttpServletRequest request) {
        UserVO loginUser = userService.getLoginUser(request);
        Long userId = loginUser.getId();
        messageService.updateReadStatus(messageId, userId, 1);
        return ResultUtils.success(true);
    }

    @DeleteMapping("/{messageId}")
    @PreAuthorizeRole("USER")
    public BaseResponse<Boolean> deleteMessage(@PathVariable Long messageId, HttpServletRequest request) {
        UserVO loginUser = userService.getLoginUser(request);
        Long userId = loginUser.getId();
        return ResultUtils.success(messageService.deleteMessage(messageId, userId));
    }

    /**
     * 发送通知接口
     *
     * @param userId  目标用户ID，如果为空表示发送给全体用户
     * @param title   通知标题
     * @param content 通知内容
     * @param request 请求对象
     * @return BaseResponse
     */
    @PostMapping("/send")
    @PreAuthorizeRole("ADMIN")
    public BaseResponse<Boolean> sendNotification(@RequestParam(required = false) Long userId,
                                                  @RequestParam String title,
                                                  @RequestParam String content,
                                                  HttpServletRequest request) {
        if (userId == null) { // 如果用户ID为空，表示发送给全体用户
            Page<UserVO> allUsersPage = userService.getAllUsers(request);
            List<UserVO> allUsers = allUsersPage.getRecords();
            allUsers.forEach(user -> sendMessage(user.getId(), title, content));
        } else {// 否则发送给指定的用户
            sendMessage(userId, title, content);
        }

        return ResultUtils.success(true);
    }

    /**
     * 发送单条通知消息
     *
     * @param userId  用户ID
     * @param title   通知标题
     * @param content 通知内容
     */
    private void sendMessage(Long userId, String title, String content) {
        Message message = new Message();
        message.setUserId(userId);
        message.setTitle(title);
        message.setContent(content);
        message.setType(MessageTypeEnum.SYSTEM_MESSAGE.getValue());
        messageService.addMessage(message);
    }

}
