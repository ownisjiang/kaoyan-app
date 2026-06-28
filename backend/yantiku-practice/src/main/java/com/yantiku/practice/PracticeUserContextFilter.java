package com.yantiku.practice;

import com.yantiku.common.annotation.InternalUserContext;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@Order(-100)
public class PracticeUserContextFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        String userIdStr = req.getHeader("X-User-Id");
        if (userIdStr != null) {
            try {
                InternalUserContext.set(Long.parseLong(userIdStr));
            } catch (NumberFormatException e) {
                log.warn("Invalid X-User-Id: {}", userIdStr);
            }
        }
        try {
            chain.doFilter(request, response);
        } finally {
            InternalUserContext.clear();
        }
    }
}
