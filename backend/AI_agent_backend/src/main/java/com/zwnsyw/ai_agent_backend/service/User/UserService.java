package com.zwnsyw.ai_agent_backend.service.User;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zwnsyw.ai_agent_backend.dto.User.UserQueryRequest;
import com.zwnsyw.ai_agent_backend.dto.User.UserUpdateRequest;
import com.zwnsyw.ai_agent_backend.entity.User.User;
import com.zwnsyw.ai_agent_backend.enums.UserEnums.UserStatusEnum;
import com.zwnsyw.ai_agent_backend.vo.User.UserVO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartFile;



public interface UserService extends IService<User> {

    /**
     * 设置角色
     *
     * @param userId   用户id
     * @param roleName 角色名
     * @return
     */
    Boolean assignRole(Long userId, String roleName);

    /**
     * 获取脱敏的已登录用户信息
     *
     * @param user 用户domain
     * @return 脱敏后的用户信息vo
     */
    UserVO convertToLoginUserVO(User user);

    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @param inviteCode    邀请码
     * @return 新用户 id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword, String inviteCode);

    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request      请求
     * @return 脱敏后的用户信息
     */
    UserVO userLoginBySession(String userAccount, String userPassword, HttpServletRequest request);


    /**
     * 获取当前登录用户信息
     *
     * @param request 请求
     * @return 当前用户
     */
    UserVO getLoginUser(HttpServletRequest request);


    /**
     * 用户注销
     *
     * @param request 请求
     * @return 注销成功与否
     */
    boolean userLogout(HttpServletRequest request);

    /**
     * 根据关键词搜索用户
     *
     * @param queryRequest 查询请求
     * @return 用户列表
     */
    Page<UserVO> getUsersByKeyword(UserQueryRequest queryRequest);

    /**
     * 获取所有用户
     *
     * @param request 请求
     * @return 用户列表
     */
    Page<UserVO> getAllUsers(HttpServletRequest request);

    /**
     * 更新用户状态
     *
     * @param userId 用户id
     * @param status 用户状态
     * @return 更新成功与否
     */
    boolean updateUserStatus(Long userId, UserStatusEnum status);

    /**
     * 逻辑删除用户
     *
     * @param userId 用户id
     * @return 删除成功与否
     */
    boolean deleteUser(Long userId);


    /**
     * 管理员重置用户密码
     *
     * @param userId          用户id
     * @param defaultPassword 默认密码
     * @return 重置成功与否
     */
    boolean resetUserPassword(Long userId, String defaultPassword);

    /**
     * 更新用户信息
     *
     * @param userUpdateRequest 用户更新请求
     * @return 更新成功与否
     */
    UserVO updateUser(UserUpdateRequest userUpdateRequest);

    /**
     * 上传头像
     *
     * @param multipartFile 文件
     * @param loginUser     登录用户
     * @return 上传结果
     */
    UserVO uploadAvatar(MultipartFile multipartFile, UserVO loginUser);

    /**
     * 更新用户密码。
     *
     * @param oldPassword 用户输入的原密码
     * @param newPassword 用户输入的新密码
     * @param request     当前的 HTTP 请求，包含用户的会话信息
     * @return 更新结果，操作成功返回 true，失败返回 false
     */
    boolean updatePassword(String oldPassword, String newPassword, HttpServletRequest request);


}