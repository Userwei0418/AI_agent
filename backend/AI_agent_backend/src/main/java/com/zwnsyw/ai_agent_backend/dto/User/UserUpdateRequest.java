package com.zwnsyw.ai_agent_backend.dto.User;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.zwnsyw.ai_agent_backend.dto.utils.UpdateGroup;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;



@Data
public class UserUpdateRequest {
    @NotNull(message = "用户ID不能为空", groups = UpdateGroup.class)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long id;

    // 用户昵称
    @Size(max = 10, message = "用户名长度不能超过10", groups = UpdateGroup.class)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String userName;

    // 性别 把gender的验证逻辑移到了service 因为如果限制用户输入影响体验 让用户 留白或者输入Male 男 0 都可以成功设置性别
    @Pattern(
            //这里也可以加个拦截 不允许其他值传入 支持的输入：数字(0,1,2)、英文(MALE,FEMALE,UNKNOWN)、中文(男,女,保密)
            regexp = "^(MALE|FEMALE|UNKNOWN|male|female|unknown|男|女|保密|0|1|2)?$",
            message = "性别字段值不合法，允许的值为 MALE|FEMALE|UNKNOWN(大小写) 0|1|2 男|女|保密 空|null",
            groups = UpdateGroup.class
    )
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String userGender;//当前逻辑 允许用户传入多种可表示gender的可能值 这些可能值是写死的 除这些值之外的值都被controller筛出 可能值里面的值会进入service进行处理 变成GenderEnum枚举存入数据库

    // 用户头像URL
    @Size(max = 255, message = "头像路径长度不能超过255字符", groups = UpdateGroup.class)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String userAvatar;

    // 电话，允许为空，空值不会通过手机号格式校验
    @Pattern(regexp = "^(1[3-9]\\d{9})?$", message = "手机号格式不正确", groups = UpdateGroup.class)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String userPhone;

    // 邮箱
    @Email(message = "邮箱格式不正确", groups = UpdateGroup.class)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String userEmail;

    // 个人简介
    @Size(max = 255, message = "个人简介长度不能超过255字符", groups = UpdateGroup.class)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String userProfile;
}
