package com.zwnsyw.ai_agent_backend.entity.User;

import com.baomidou.mybatisplus.annotation.*;
import com.zwnsyw.ai_agent_backend.enums.UserEnums.UserGenderEnum;
import com.zwnsyw.ai_agent_backend.enums.UserEnums.UserStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@TableName(value = "user")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 密码
     */
    private String userPassword;

    /**
     * 动态盐值
     */
    private String salt;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 性别
     */
    private UserGenderEnum userGender;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 电话
     */
    private String userPhone;

    /**
     * 邮箱
     */
    private String userEmail;

    /**
     * 用户简介
     */
    private String userProfile;

    /**
     * 邀请码
     */
    private String shareCode;

    /**
     * 邀请用户ID
     */
    private Long inviteUserId;

    /**
     * 编辑时间
     */
    private Date editTime;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 更新时间
     */
    private UserStatusEnum userStatus;
    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}