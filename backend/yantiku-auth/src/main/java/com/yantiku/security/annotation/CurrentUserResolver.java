package com.yantiku.security.annotation;

import com.yantiku.common.annotation.CurrentUser;
import com.yantiku.common.exception.BusinessException;
import com.yantiku.module.user.model.entity.User;
import com.yantiku.module.user.mapper.UserMapper;
import com.yantiku.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.MethodParameter;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class CurrentUserResolver implements HandlerMethodArgumentResolver, WebMvcConfigurer {

    private final UserMapper userMapper;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUser.class)
                && (parameter.getParameterType().equals(Long.class)
                    || parameter.getParameterType().equals(User.class));
    }

    @Override
    public Object resolveArgument(
            @NonNull MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            @NonNull NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory) {

        Long userId = SecurityUtils.getCurrentUserId();

        if (userId == null) {
            throw new BusinessException("User not authenticated");
        }

        // If the parameter is Long, return the userId directly
        if (parameter.getParameterType().equals(Long.class)) {
            return userId;
        }

        // Otherwise, fetch and return the User entity
        User user = userMapper.findById(userId);

        if (user == null || user.getDeletedAt() != null) {
            throw new BusinessException("User not found or deleted");
        }

        return user;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(this);
    }
}
