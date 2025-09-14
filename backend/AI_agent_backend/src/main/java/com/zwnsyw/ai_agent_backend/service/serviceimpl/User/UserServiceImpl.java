package com.zwnsyw.ai_agent_backend.service.serviceimpl.User;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zwnsyw.ai_agent_backend.common.request.PageRequest;
import com.zwnsyw.ai_agent_backend.dto.File.UploadPictureResult;
import com.zwnsyw.ai_agent_backend.dto.User.UserQueryRequest;
import com.zwnsyw.ai_agent_backend.dto.User.UserUpdateRequest;
import com.zwnsyw.ai_agent_backend.entity.Message.Message;
import com.zwnsyw.ai_agent_backend.entity.User.Role;
import com.zwnsyw.ai_agent_backend.entity.User.User;
import com.zwnsyw.ai_agent_backend.entity.User.UserRole;
import com.zwnsyw.ai_agent_backend.enums.MessageEnums.MessageTypeEnum;
import com.zwnsyw.ai_agent_backend.enums.UserEnums.UserGenderEnum;
import com.zwnsyw.ai_agent_backend.enums.UserEnums.UserStatusEnum;
import com.zwnsyw.ai_agent_backend.exception.BusinessException;
import com.zwnsyw.ai_agent_backend.exception.ErrorCode;
import com.zwnsyw.ai_agent_backend.exception.ThrowUtils;
import com.zwnsyw.ai_agent_backend.manager.uploadBySteam.FilePictureUploadBySteam;
import com.zwnsyw.ai_agent_backend.manager.uploadBySteam.PictureUploadBySteamTemplate;
import com.zwnsyw.ai_agent_backend.mapper.User.RoleMapper;
import com.zwnsyw.ai_agent_backend.mapper.User.UserMapper;
import com.zwnsyw.ai_agent_backend.mapper.User.UserRoleMapper;
import com.zwnsyw.ai_agent_backend.service.Message.MessageService;
import com.zwnsyw.ai_agent_backend.service.User.PermissionService;
import com.zwnsyw.ai_agent_backend.service.User.UserService;
import com.zwnsyw.ai_agent_backend.utils.PasswordUtils;
import com.zwnsyw.ai_agent_backend.vo.User.UserVO;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.session.SessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


import java.lang.reflect.Field;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.zwnsyw.ai_agent_backend.contant.UserConstant.DEFAULT_ROLE;
import static com.zwnsyw.ai_agent_backend.contant.UserConstant.USER_LOGIN_STATE;


@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private RoleMapper roleMapper;

    @Resource
    private UserRoleMapper userRoleMapper;

    @Resource
    private PasswordUtils passwordUtils;

    @Resource
    private PermissionService permissionService;

    @Resource
    private MessageService messageService;

    @Resource
    private FilePictureUploadBySteam filePictureUploadBySteam;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private SessionRepository<? extends Session> sessionRepository;

    private static final String PASSWORD_RETRY_KEY = "ai_agent:user:password_retry:%s";

    @Value("${user.max-password-retry}")
    private int maxPwdRetryLimit;

    @Value("${user.max-login-device}")
    private int maxDeviceLimit;

    private String generateRandomSalt() {
        byte[] salt = new byte[16];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    private boolean isAccountExist(String userAccount) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        return userMapper.selectCount(queryWrapper) > 0;
    }

    private Long getRoleIdByRoleName(String roleName) {
        QueryWrapper<Role> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("name", roleName);
        Role role = roleMapper.selectOne(queryWrapper);
        return role != null ? role.getId() : null;
    }

    @Override
    @Transactional
    public Boolean assignRole(Long userId, String roleName) {
        Long roleId = getRoleIdByRoleName(roleName);
        ThrowUtils.throwIf(roleId == null, ErrorCode.SYSTEM_ERROR, "角色不存在: " + roleName);

        User user = userMapper.selectById(userId);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR, "用户不存在");

        // 检查用户是否已经拥有该角色
        QueryWrapper<UserRole> userRoleQueryWrapper = new QueryWrapper<>();
        userRoleQueryWrapper.eq("userId", userId).eq("roleId", roleId);
        UserRole existingUserRole = userRoleMapper.selectOne(userRoleQueryWrapper);
        ThrowUtils.throwIf(existingUserRole != null, ErrorCode.PARAMS_ERROR, "用户已经拥有该角色");

        UserRole userRole = new UserRole();
        userRole.setUserId(userId);
        userRole.setRoleId(roleId);
        int result = userRoleMapper.insert(userRole);
        return result > 0;
    }

    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword, String inviteCode) {
        //todo debug 用户被逻辑删除 账号仍被占用 这里检测不到 可以进入数据库插入操作 但会触发唯一键冲突 无法注册 报错只会显示系统异常
        ThrowUtils.throwIf(isAccountExist(userAccount), ErrorCode.PARAMS_ERROR, "账户已存在");

        Long inviteUserId = null;
        if (inviteCode != null && !inviteCode.isEmpty()) {
            User invitedUser = userMapper.selectByShareCode(inviteCode);
            ThrowUtils.throwIf(invitedUser == null, ErrorCode.PARAMS_ERROR, "邀请码不存在");
            inviteUserId = invitedUser.getId();

            // 通知邀请者：有新用户使用其邀请码注册
            Message inviteMessage = new Message();
            inviteMessage.setUserId(inviteUserId);
            inviteMessage.setTitle("邀请成功");
            inviteMessage.setContent("用户" + userAccount + "已通过您的邀请码注册成功");
            inviteMessage.setType(MessageTypeEnum.SYSTEM_MESSAGE.getValue());
            messageService.addMessage(inviteMessage);
        }

        String dynamicSalt = generateRandomSalt();
        String encryptedPassword = passwordUtils.encryptPassword(userPassword, dynamicSalt);

        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptedPassword);
        user.setSalt(dynamicSalt);
        user.setUserName(generateRandomUsername());
        user.setInviteUserId(inviteUserId);
        user.setShareCode(generateRandomShareCode());

        boolean saveResult = this.save(user);
        ThrowUtils.throwIf(!saveResult, ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");

        assignRole(user.getId(), DEFAULT_ROLE);

        // 发送新用户欢迎消息
        Message welcomeMessage = new Message();
        welcomeMessage.setUserId(user.getId());
        welcomeMessage.setTitle("欢迎加入");
        welcomeMessage.setContent("感谢注册，祝您在我们的AI智能体平台玩得愉快！");
        welcomeMessage.setType(MessageTypeEnum.SYSTEM_MESSAGE.getValue());
        messageService.addMessage(welcomeMessage);

        return user.getId();
    }


    private User getUserByAccount(String userAccount) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        User user = userMapper.selectOne(queryWrapper);

        ThrowUtils.throwIf(user == null, ErrorCode.PARAMS_ERROR, "账户不存在");
        ThrowUtils.throwIf(UserStatusEnum.DISABLED.equals(user.getUserStatus()), ErrorCode.NULL_ERROR, "该账号已被封禁");

        return user;
    }

    private String generateRandomUsername() {
        return "AI_" + RandomUtil.randomString(8);
    }

    private String generateRandomShareCode() {
        return RandomUtil.randomString(10);
    }

    private Long getInviteUserNumber(Long userId) {
        QueryWrapper<User> inviteUserQueryWrapper = new QueryWrapper<>();
        inviteUserQueryWrapper.eq("inviteUserId", userId);
        return userMapper.selectCount(inviteUserQueryWrapper);
    }

    private List<String> getUserRoleNamesByUserId(Long userId) {
        return userRoleMapper.getUserRoleNamesByUserId(userId);
    }

    private Set<String> getUserPermissions(Long userId) {
        return permissionService.getMenuPermission(userId);
    }

    public UserVO convertToLoginUserVO(User user) {
        Long inviteUserNumber = getInviteUserNumber(user.getId());
        List<String> roles = getUserRoleNamesByUserId(user.getId());
        Set<String> permissions = getUserPermissions(user.getId());
        return UserVO.fromUser(user, inviteUserNumber, roles, permissions);
    }

    //    @Override
//    public UserVO userLoginBySession(String userAccount, String userPassword, HttpServletRequest request) {
//
//        User user = getUserByAccount(userAccount);
//
//        // to do done 接入redis 实现密码重试过多锁定
//        boolean isPasswordValid = passwordUtils.verifyPassword(userPassword, user.getUserPassword(), user.getSalt());
//        ThrowUtils.throwIf(!isPasswordValid, ErrorCode.PARAMS_ERROR, "密码错误");
//
//        UserVO LoginuserVO = convertToLoginUserVO(user);
//
//        // to do done 接入redis 用redis维护session 且实现分布式登录 登陆设备数量可控
//        request.getSession().setAttribute(USER_LOGIN_STATE, LoginuserVO);
//
//        return LoginuserVO;
//    }
    @Override
    public UserVO userLoginBySession(String userAccount, String userPassword, HttpServletRequest request) {

        User user = getUserByAccount(userAccount);

        //to do 密码重试过多锁定 (redis) done
        String retryKey = String.format(PASSWORD_RETRY_KEY, userAccount);
        Integer retryCount = (Integer) redisTemplate.opsForValue().get(retryKey);

        if (retryCount != null && retryCount >= maxPwdRetryLimit) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "密码输入次数过多，请10分钟后重试");
        }

        boolean isPasswordValid = passwordUtils.verifyPassword(userPassword, user.getUserPassword(), user.getSalt());
        if (!isPasswordValid) {
            // 密码错误时记录次数
            redisTemplate.opsForValue().set(retryKey, retryCount != null ? retryCount + 1 : 1, 600, TimeUnit.SECONDS);
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
        } else {
            // 登录成功重置计数器
            redisTemplate.delete(retryKey);
        }

        UserVO LoginuserVO = convertToLoginUserVO(user);

        // 获取用户 IP 和会话 SessionId
        String userIp = request.getRemoteAddr();
        String sessionId = request.getSession().getId();
        // 验证设备数量，并记录登录设备
        if (!recordUserDevice(user.getId(), sessionId, userIp)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "设备登录数量超限制");
        }

        //to do 引入redis 控制登入设备数量 done
        request.getSession().setAttribute(USER_LOGIN_STATE, LoginuserVO);
        log.info("用户登录成功: userId={}, account={}, sessionId={}, ip={}", user.getId(), userAccount, sessionId, userIp);

        return LoginuserVO;
    }

    /**
     * 记录用户设备信息，同时清理旧设备的登录状态
     */
    private boolean recordUserDevice(long userId, String sessionId, String userIp) {
        String redisKey = "login_devices:" + userId;
        String deviceKey = sessionId + "_" + userIp;

        // 使用 ZSet 存储设备信息，并以时间戳为权重
        long currentTime = System.currentTimeMillis();
        redisTemplate.opsForZSet().add(redisKey, deviceKey, currentTime);

        // 检查设备数量是否超出限制
        Long deviceCountObj = redisTemplate.opsForZSet().size(redisKey);
        long deviceCount = deviceCountObj != null ? deviceCountObj : 0;
        if (deviceCount > maxDeviceLimit) {
            log.warn("超过设备数量限制: userId={}, deviceCount={}, maxDeviceLimit={}", userId, deviceCount, maxDeviceLimit);

            // 获取超出部分的旧设备
            Set<Object> devicesToRemove = redisTemplate.opsForZSet()
                    .range(redisKey, 0, deviceCount - maxDeviceLimit - 1);

            if (devicesToRemove != null) {
                for (Object device : devicesToRemove) {
                    String[] parts = device.toString().split("_");
                    if (parts.length > 0) {
                        String oldestSessionId = parts[0];

                        // 销毁旧设备登录态
                        invalidateSession(oldestSessionId);

                        Message message = new Message();
                        message.setUserId(userId);
                        message.setTitle("下线通知");
                        message.setContent("您的账户已达到最大设备登录数量限制，已自动下线最早登入设备"); //todo session（vo)中记录ip 登录地点 优化通知
                        message.setType(MessageTypeEnum.SYSTEM_MESSAGE.getValue());
                        messageService.addMessage(message);

                    }
                    // 从 Redis 中移除旧设备
                    redisTemplate.opsForZSet().remove(redisKey, device);
                }
            }
        }

        // 设置 Redis 键的过期时间
        redisTemplate.expire(redisKey, 7, TimeUnit.DAYS);
        return true;
    }

    /**
     * 销毁给定会话的 HttpSession
     */
    private void invalidateSession(String sessionId) {
        // 使用分布式 Session 管理销毁会话
        Session session = sessionRepository.findById(sessionId);
        if (session != null) {
            sessionRepository.deleteById(sessionId); // 强制删除 Redis 中的 Session
            log.info("Session 已销毁: sessionId={}", sessionId);
        } else {
            log.warn("未能找到 Session: sessionId={}", sessionId);
        }
    }

    @Override
    public UserVO getLoginUser(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        ThrowUtils.throwIf(userObj == null, ErrorCode.NOT_LOGIN_ERROR, "未登录或会话已过期");
        return (UserVO) userObj;
    }


//    @Override
//    public boolean userLogout(HttpServletRequest request) {
//        if (getLoginUser(request) != null)
//            request.getSession().removeAttribute(USER_LOGIN_STATE);
//        return true;
//    }

    @Override
    public boolean userLogout(HttpServletRequest request) {
        // 获取当前用户ID
        UserVO loginUser = (UserVO) request.getSession().getAttribute(USER_LOGIN_STATE);
        if (loginUser != null) {
            String redisKey = "login_devices:" + loginUser.getId();
            String sessionId = request.getSession().getId();
            String userIp = request.getRemoteAddr();

            // 移除设备记录
            String deviceKey = sessionId + "_" + userIp;
            redisTemplate.opsForZSet().remove(redisKey, deviceKey);

            log.info("用户注销成功: userId={}, sessionId={}, userIp={}", loginUser.getId(), sessionId, userIp);
        }

        // 销毁 Session
        request.getSession().invalidate();
        return true;
    }


    private boolean isNumeric(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        for (char c : str.toCharArray()) {
            if (!Character.isDigit(c)) {
                return false;
            }
        }
        return true;
    }

    private void addQueryConditions(QueryWrapper<User> queryWrapper, UserQueryRequest queryRequest) {
        if (StringUtils.isNotBlank(queryRequest.getKeyword())) {
            if (isNumeric(queryRequest.getKeyword())) {
                // 如果 keyword 是纯数字，则同时进行 id 和 like 查询
                queryWrapper.and(qw ->
                        qw.nested(subQw ->
                                subQw.eq("id", Long.parseLong(queryRequest.getKeyword()))
                                        .or()
                                        .like("userName", queryRequest.getKeyword())
                                        .or()
                                        .like("userAccount", queryRequest.getKeyword()))
                );
            } else {
                // 如果 keyword 不是纯数字，则只进行 like 查询
                queryWrapper.and(qw ->
                        qw.nested(subQw ->
                                subQw.like("userName", queryRequest.getKeyword())
                                        .or()
                                        .like("userAccount", queryRequest.getKeyword()))
                );
            }
        }
    }

    @Override
    public Page<UserVO> getUsersByKeyword(UserQueryRequest queryRequest) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        addQueryConditions(queryWrapper, queryRequest);

        Page<User> page = new Page<>(queryRequest.getPage(), queryRequest.getPageSize());

        Page<User> userPage = this.page(page, queryWrapper);

        Page<UserVO> loginUserPage = new Page<>(userPage.getCurrent(), userPage.getSize(), userPage.getTotal());
        loginUserPage.setRecords(userPage.getRecords().stream()
                .map(this::convertToLoginUserVO)
                .collect(Collectors.toList()));

        return loginUserPage;
    }


    @Override
    public Page<UserVO> getAllUsers(HttpServletRequest request) {

        PageRequest pageRequest = PageRequest.fromRequest(request);
        Page<User> page = new Page<>(pageRequest.getPage(), pageRequest.getPageSize());

        Page<User> userPage = this.page(page);

        Page<UserVO> loginUserPage = new Page<>(userPage.getCurrent(), userPage.getSize(), userPage.getTotal());
        loginUserPage.setRecords(userPage.getRecords().stream()
                .map(this::convertToLoginUserVO)
                .collect(Collectors.toList()));

        return loginUserPage;
    }


    @Override
    public boolean updateUserStatus(Long userId, UserStatusEnum status) {
        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", userId).set("userStatus", status);
        return update(updateWrapper);
    }


    @Override
    public boolean deleteUser(Long userId) {
        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(User::getId, userId)
                .set(User::getIsDelete, 1); // 设置 isDelete 为 1 表示逻辑删除
        return update(updateWrapper);
    }


    @Override
    public boolean resetUserPassword(Long userId, String defaultPassword) {
        String dynamicSalt = generateRandomSalt();
        String encryptedPassword = passwordUtils.encryptPassword(defaultPassword, dynamicSalt);

        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", userId)
                .set("userPassword", encryptedPassword)
                .set("salt", dynamicSalt);

        // 发送消息
        Message welcomeMessage = new Message();
        welcomeMessage.setUserId(userId);
        welcomeMessage.setTitle("密码重置");
        welcomeMessage.setContent("您的密码已经重置为“ai_agent");
        welcomeMessage.setType(MessageTypeEnum.SYSTEM_MESSAGE.getValue());
        messageService.addMessage(welcomeMessage);

        return update(updateWrapper);
    }


    private String normalizeGender(String gender) {
        if (gender == null || gender.trim().isEmpty()) {
            return "UNKNOWN";
        }

        Map<String, String> genderMapping = new HashMap<>();
        genderMapping.put("男", "MALE");
        genderMapping.put("male", "MALE");
        genderMapping.put("男性", "MALE");
        genderMapping.put("0", "MALE");
        genderMapping.put("女", "FEMALE");
        genderMapping.put("female", "FEMALE");
        genderMapping.put("女性", "FEMALE");
        genderMapping.put("1", "FEMALE");
        genderMapping.put("未知", "UNKNOWN");
        genderMapping.put("保密", "UNKNOWN");
        genderMapping.put("unknown", "UNKNOWN");
        genderMapping.put("2", "UNKNOWN");

        return genderMapping.getOrDefault(gender.toLowerCase().trim(), gender);
    }

    @Override
    public UserVO updateUser(UserUpdateRequest userUpdateRequest) {

        User oldUser = userMapper.selectById(userUpdateRequest.getId());
        ThrowUtils.throwIf(oldUser == null, ErrorCode.NOT_FOUND_ERROR, "用户不存在");

        Map<String, Object> updateFields = new HashMap<>();

        if (org.springframework.util.StringUtils.hasText(userUpdateRequest.getUserName()) && !userUpdateRequest.getUserName().equals(oldUser.getUserName())) {
            updateFields.put("userName", userUpdateRequest.getUserName());
        }
        if (org.springframework.util.StringUtils.hasText(userUpdateRequest.getUserAvatar()) && !userUpdateRequest.getUserAvatar().equals(oldUser.getUserAvatar())) {
            updateFields.put("userAvatar", userUpdateRequest.getUserAvatar());
        }
        if (org.springframework.util.StringUtils.hasText(userUpdateRequest.getUserGender())) {
            UserGenderEnum newGender = UserGenderEnum.fromCode(normalizeGender(userUpdateRequest.getUserGender()));
            UserGenderEnum currentGender = oldUser.getUserGender();
            if (!newGender.equals(currentGender)) {
                updateFields.put("userGender", newGender);
            }
        }
        if (org.springframework.util.StringUtils.hasText(userUpdateRequest.getUserPhone()) && !userUpdateRequest.getUserPhone().equals(oldUser.getUserPhone())) {
            updateFields.put("userPhone", userUpdateRequest.getUserPhone());
        }
        if (org.springframework.util.StringUtils.hasText(userUpdateRequest.getUserEmail()) && !userUpdateRequest.getUserEmail().equals(oldUser.getUserEmail())) {
            updateFields.put("userEmail", userUpdateRequest.getUserEmail());
        }
        if (org.springframework.util.StringUtils.hasText(userUpdateRequest.getUserProfile()) && !userUpdateRequest.getUserProfile().equals(oldUser.getUserProfile())) {
            updateFields.put("userProfile", userUpdateRequest.getUserProfile());
        }

        ThrowUtils.throwIf(updateFields.isEmpty(), ErrorCode.PARAMS_ERROR, "没有需要更新的字段");

        User updatedUser = new User();
        updatedUser.setId(userUpdateRequest.getId());
        // 使用反射机制更新字段
        updateFields.forEach((key, value) -> {
            try {
                // 使用反射给 User 对象的字段赋值
                Field field = User.class.getDeclaredField(key);
                field.setAccessible(true);
                field.set(updatedUser, value);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "字段更新失败：" + key);
            }
        });

        int result = userMapper.updateById(updatedUser);
        ThrowUtils.throwIf(result <= 0, ErrorCode.SYSTEM_ERROR, "用户信息更新失败，请稍后重试");

        User NewUser = userMapper.selectById(userUpdateRequest.getId());
        return convertToLoginUserVO(NewUser);
    }

    @Override
    public UserVO uploadAvatar(MultipartFile file, UserVO loginUser) {

        String uploadPathPrefix = String.format("AiAgent/Avatar/%s", loginUser.getId());

        UploadPictureResult uploadResult = filePictureUploadBySteam.uploadPicture(file, uploadPathPrefix);
        String avatarUrl = uploadResult.getUrl();

        UserUpdateRequest updateRequest = new UserUpdateRequest();
        updateRequest.setId(loginUser.getId());
        updateRequest.setUserAvatar(avatarUrl);

        return updateUser(updateRequest);
    }


    @Override
    public boolean updatePassword(String oldPassword, String newPassword, HttpServletRequest request) {

        UserVO loginUser = getLoginUser(request);
        User currentUser = this.getById(loginUser.getId());
        ThrowUtils.throwIf(currentUser == null, ErrorCode.NOT_FOUND_ERROR, "用户不存在");

        ThrowUtils.throwIf(
                !passwordUtils.verifyPassword(oldPassword, currentUser.getUserPassword(), currentUser.getSalt()),
                ErrorCode.OPERATION_ERROR,
                "旧密码不正确");

        ThrowUtils.throwIf(
                passwordUtils.verifyPassword(newPassword, currentUser.getUserPassword(), currentUser.getSalt()),
                ErrorCode.OPERATION_ERROR,
                "新密码不能与旧密码相同");

        String dynamicSalt = generateRandomSalt();

        String encryptedPassword = passwordUtils.encryptPassword(newPassword, dynamicSalt);

        currentUser.setUserPassword(encryptedPassword);
        currentUser.setSalt(dynamicSalt);

        boolean updateResult = this.updateById(currentUser);
        ThrowUtils.throwIf(!updateResult, ErrorCode.SYSTEM_ERROR, "密码更新失败");

        //修改用户密码可以不用清除缓存 不会影响推荐等前端展示 不过登录态可能需要移除 直接执行一次退出登入即可
        userLogout(request);

        return true;
    }


}