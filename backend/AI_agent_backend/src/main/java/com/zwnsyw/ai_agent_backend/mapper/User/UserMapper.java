package com.zwnsyw.ai_agent_backend.mapper.User;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zwnsyw.ai_agent_backend.entity.User.User;
import org.apache.ibatis.annotations.Param;


public interface UserMapper extends BaseMapper<User> {

    User selectByShareCode(@Param("shareCode") String shareCode);

    User selectByUserAccount(String userAccount);
}



