package com.zwnsyw.ai_agent_backend.service.serviceimpl.Message;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zwnsyw.ai_agent_backend.entity.Message.Message;
import com.zwnsyw.ai_agent_backend.exception.ErrorCode;
import com.zwnsyw.ai_agent_backend.exception.ThrowUtils;
import com.zwnsyw.ai_agent_backend.mapper.Message.MessageMapper;
import com.zwnsyw.ai_agent_backend.service.Message.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;


@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message>
        implements MessageService {

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private SseEmitterRepository emitterRepository;

    @Override
    public void addMessage(Message message) {
        message.setCreateTime(new Date());
        messageMapper.insert(message);
        // 推送消息给用户
        emitterRepository.pushMessage(message.getUserId(), message);
    }

    @Override
    public List<Message> getMessages(Long userId) {

        QueryWrapper<Message> query = new QueryWrapper<>();
        query.eq("userId", userId);
        query.orderByDesc("createTime");

        return messageMapper.selectList(query);
    }

    @Override
    public void updateReadStatus(Long messageId, Long userId, Integer isRead) {
        // 构建查询条件：消息ID和用户ID必须同时匹配
        QueryWrapper<Message> query = new QueryWrapper<>();
        query.eq("id", messageId)
                .eq("userId", userId);

        // 更新isRead字段
        Message update = new Message();
        update.setIsRead(isRead);
        messageMapper.update(update, query);

        // 校验更新是否成功（可选）
        ThrowUtils.throwIf(
                messageMapper.selectCount(query) == 0,
                ErrorCode.NO_AUTH_ERROR,
                "消息不存在或无权操作"
        );
    }

    @Override
    public boolean deleteMessage(Long messageId, Long userId) {

        QueryWrapper<Message> query = new QueryWrapper<>();
        query.eq("id", messageId)
                .eq("userId", userId);
        Message message = messageMapper.selectOne(query);
        ThrowUtils.throwIf(message == null, ErrorCode.NO_AUTH_ERROR, "消息不存在或无权删除");

        int rows = messageMapper.deleteById(messageId);
        return rows > 0;
    }
}