package com.zwnsyw.ai_agent_backend.service.serviceimpl.User;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zwnsyw.ai_agent_backend.entity.User.UserRole;
import com.zwnsyw.ai_agent_backend.mapper.User.UserRoleMapper;
import com.zwnsyw.ai_agent_backend.service.User.UserRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserRoleServiceImpl extends ServiceImpl<UserRoleMapper, UserRole> implements UserRoleService {
    @Autowired
    private UserRoleMapper userRoleMapper;
    public List<UserRole> getUserRolesByUserId(Long userId) {
        return userRoleMapper.getUserRolesByUserId(userId);
    }
}
