package com.yantiku.question;

import com.yantiku.common.annotation.InternalUserContext;
import com.yantiku.common.exception.BusinessException;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 管理员权限拦截 — 仅 admin 可执行题库写操作
 */
@Component
@Order(2)
public class AdminAuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        String method = req.getMethod();
        String path = req.getRequestURI();

        // 题库写操作需要管理员权限
        if (isAdminWriteOp(method, path)) {
            if (!InternalUserContext.isAdmin()) {
                throw new BusinessException(HttpStatus.FORBIDDEN.value(), "需要管理员权限");
            }
        }

        chain.doFilter(request, response);
    }

    private boolean isAdminWriteOp(String method, String path) {
        if (!path.startsWith("/api/v1/questions")) return false;
        return "POST".equalsIgnoreCase(method) 
            || "PUT".equalsIgnoreCase(method) 
            || "DELETE".equalsIgnoreCase(method);
    }
}
