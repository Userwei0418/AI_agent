package com.zwnsyw.ai_agent_backend.service.Message;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zwnsyw.ai_agent_backend.entity.Message.Message;

import java.util.List;


public interface MessageService extends IService<Message> {
    /**
     * 添加消息
     */
    void addMessage(Message message);

    /**
     * 获取消息
     */
    List<Message> getMessages(Long userId);

    /**
     * 修改消息状态
     */
    void updateReadStatus(Long messageId, Long userId, Integer isRead);

    /**
     * 删除消息
     */
    boolean deleteMessage(Long messageId, Long userId);
}
