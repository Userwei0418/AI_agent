package com.zwnsyw.ai_agent_backend.RBAC.interceptor;

import com.zwnsyw.ai_agent_backend.RBAC.annotation.PreAuthorize;
import com.zwnsyw.ai_agent_backend.RBAC.annotation.PreAuthorizeRole;
import com.zwnsyw.ai_agent_backend.RBAC.properties.PermitAllUrlProperties;
import com.zwnsyw.ai_agent_backend.exception.BusinessException;
import com.zwnsyw.ai_agent_backend.exception.ErrorCode;
import com.zwnsyw.ai_agent_backend.service.User.UserService;
import com.zwnsyw.ai_agent_backend.vo.User.UserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.List;

import static com.zwnsyw.ai_agent_backend.contant.UserConstant.ADMIN_ROLE;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Autowired
    private UserService userService;

    @Autowired
    @Lazy
    private PermitAllUrlProperties permitAllUrlProperties;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            Method method = handlerMethod.getMethod();

            // 检查是否为匿名访问
            if (isAnonymousAccess(request)) {
                return true;
            }

            PreAuthorize preAuthorize = method.getAnnotation(PreAuthorize.class);
            if (preAuthorize != null) {
                String permission = preAuthorize.value();
                if (!hasPermission(request, permission)) {
                    throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "权限不足");
                }
            }

            PreAuthorizeRole preAuthorizeRole = method.getAnnotation(PreAuthorizeRole.class);
            if (preAuthorizeRole != null) {
                String role = preAuthorizeRole.value();
                if (!hasRole(request, role)) {
                    throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "身份不足");
                }
            }
        }
        return true;
    }

    private boolean isAnonymousAccess(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        List<String> permitAllUrls = permitAllUrlProperties.getUrls();
        for (String url : permitAllUrls) {
            if (requestURI.matches(url)) {
                return true;
            }
        }
        return false;
    }
    private boolean hasPermission(HttpServletRequest request, String permission) {
        UserVO loginUser = userService.getLoginUser(request);
        if (loginUser == null || loginUser.getPermissions() == null) {
            return false;
        }
        if (loginUser.getPermissions().contains("*:*:*")) {
            return true;
        }
        return loginUser.getPermissions().contains(permission);
    }

    private boolean hasRole(HttpServletRequest request, String role) {
        UserVO loginUser = userService.getLoginUser(request);
        if (loginUser == null || loginUser.getRoles() == null) {
            return false;
        }
        if (loginUser.getRoles().contains(ADMIN_ROLE)) {
            return true;
        }
        return loginUser.getRoles().contains(role);
    }
}
