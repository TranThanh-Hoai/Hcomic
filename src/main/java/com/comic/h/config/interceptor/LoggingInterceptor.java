package com.comic.h.config.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class LoggingInterceptor implements HandlerInterceptor {

    private final ThreadLocal<Long> startTime = new ThreadLocal<>();

    private static final Logger log = LoggerFactory.getLogger(LoggingInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request,
            HttpServletResponse response,
            Object handler) {

        startTime.set(System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            Exception ex) {

        long duration = System.currentTimeMillis() - startTime.get();

        int status = response.getStatus();

        if (status >= 500) {
            log.error("{} {} {} ({} ms)",
                    request.getMethod(),
                    request.getRequestURI(),
                    status,
                    duration);
        } else if (status >= 400) {
            log.warn("{} {} {} ({} ms)",
                    request.getMethod(),
                    request.getRequestURI(),
                    status,
                    duration);
        } else {
            log.info("{} {} {} ({} ms)",
                    request.getMethod(),
                    request.getRequestURI(),
                    status,
                    duration);
        }

        startTime.remove();
    }
}
