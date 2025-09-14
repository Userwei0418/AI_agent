package com.zwnsyw.ai_agent_backend.task;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zwnsyw.ai_agent_backend.entity.Message.Message;
import com.zwnsyw.ai_agent_backend.mapper.Message.MessageMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Component
public class MessageCleanupTask {

    @Autowired
    private MessageMapper messageMapper;

    // 每天凌晨2点执行
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void cleanupExpiredMessages() {
        // 计算30天前的时间
        Date thirtyDaysAgo = new Date(System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000);

        // 构建查询条件：已读且创建时间早于30天前
        QueryWrapper<Message> query = new QueryWrapper<>();
        query.eq("isRead", 1)
             .lt("createTime", thirtyDaysAgo);

        // 执行删除（物理删除）
        messageMapper.delete(query);
    }
}