package com.zwnsyw.ai_agent_backend.mapper.User;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zwnsyw.ai_agent_backend.entity.User.UserRole;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;


public interface UserRoleMapper extends BaseMapper<UserRole> {
    @Select("SELECT ur.roleId, r.name AS role_name " +
            "FROM user_role ur " +
            "JOIN role r ON ur.roleId = r.id " +
            "WHERE ur.userId = #{userId}")
    List<UserRole> getUserRolesByUserId(@Param("userId") Long userId);

    @Select("SELECT r.name AS role_name " +
            "FROM user_role ur " +
            "JOIN role r ON ur.roleId = r.id " +
            "WHERE ur.userId = #{userId}")
    List<String> getUserRoleNamesByUserId(@Param("userId") Long userId);

}



