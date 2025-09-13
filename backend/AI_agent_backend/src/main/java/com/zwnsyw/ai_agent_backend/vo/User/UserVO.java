package com.zwnsyw.ai_agent_backend.vo.User;

import com.zwnsyw.ai_agent_backend.entity.User.User;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Data
public class UserVO implements Serializable {
    private Long id;  // 用户ID

    private String userAccount;  // 账号

    private String userName;  // 用户昵称

    private String userGender;  // 性别（转换为描述性字符串）

    private String userAvatar;  // 用户头像

    private String userPhone;  // 脱敏后的电话

    private String userEmail;  // 脱敏后的邮箱

    private String userProfile; // 个人简介

    private String shareCode;  // 邀请码

    private Long inviteUserNumber; // 邀请用户数量

    private Date createTime;  // 创建时间

    private Date updateTime;  // 更新时间

    private List<String> roles; //身份列表

    private Set<String> permissions; // 权限列表

    private String userStatus;  // 用户状态（转换为描述性字符串）
    private static final long serialVersionUID = 1L;

    // 图床基础路径（作为完整 URL 拼接的前缀）
//    private static final String BASE_IMAGE_URL = "http://localhost:8123/api";
    private static final String BASE_IMAGE_URL = "https://zwnsyw-1318445231.cos.ap-guangzhou.myqcloud.com/";

    public static UserVO fromUser(User user, Long inviteUserNumber, List<String> roles, Set<String> permissions) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUserAccount(user.getUserAccount());
        vo.setUserName(user.getUserName());
        vo.setUserGender(user.getUserGender() != null ? user.getUserGender().getDescription() : null);

        if (user.getUserAvatar() != null) {
            if (isFullUrl(user.getUserAvatar())) {
                vo.setUserAvatar(user.getUserAvatar());
            } else {
                vo.setUserAvatar(BASE_IMAGE_URL + user.getUserAvatar());
            }
        }

        vo.setUserPhone(maskPhone(user.getUserPhone()));
        vo.setUserEmail(maskEmail(user.getUserEmail()));
        vo.setUserProfile(user.getUserProfile());
        vo.setShareCode(user.getShareCode());
        vo.setInviteUserNumber(inviteUserNumber);
        vo.setCreateTime(user.getCreateTime());
        vo.setUpdateTime(user.getUpdateTime());
        vo.setRoles(roles);
        vo.setPermissions(permissions);
        vo.setUserStatus(user.getUserStatus() != null ? user.getUserStatus().getDescription() : null);
        return vo;
    }

    // 判断是否为完整 URL（如以 http:// 或 https:// 开头）
    private static boolean isFullUrl(String url) {
        return url.startsWith("http://") || url.startsWith("https://");
    }

    // 脱敏逻辑封装 for Phone
    private static String maskPhone(String phone) {
        return phone != null ? phone.replaceAll("(?<=\\d{3})\\d{4}(?=\\d{4})", "****") : null;
    }

    // 脱敏逻辑封装 for Email
    private static String maskEmail(String email) {
        return email != null ? email.replaceAll("(?<=\\w{2})\\w+(?=@)", "****") : null;
    }
}
