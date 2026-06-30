package com.record.common.aspect;

import com.record.common.filter.CachingRequestResponseFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;
import org.springframework.web.util.WebUtils;

import java.nio.charset.StandardCharsets;

/**
 * Controller 层请求/响应日志切面。
 * <p>通过 {@link CachingRequestResponseFilter} 缓存的 request/response wrapper
 * 获取请求体和响应体内容，每个请求输出三行日志：
 * <pre>
 * [http] >> POST /api/xxx
 * [http] >> body={"key":"value"}
 * [http] << {"code":0,"data":...}
 * </pre>
 * GET/DELETE 不输出 body 行。超长 body 截断 1000 字符。
 * </p>
 */
@Aspect
@Component
public class RequestLogAspect {

    private static final Logger log = LoggerFactory.getLogger(RequestLogAspect.class);
    private static final int BODY_MAX_LEN = 1000;

    @Around("execution(* com.record..controller..*.*(..))")
    public Object logRequest(ProceedingJoinPoint joinPoint) throws Throwable {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        // 方法签名
        String method = "?";
        String uri = "?";
        if (attrs != null) {
            HttpServletRequest req = attrs.getRequest();
            method = req.getMethod();
            uri = req.getRequestURI();
        }

        // 打印请求行（方法 + URI）
        log.info("[http] >> {} {}", method, uri);

        // 打印请求体
        String reqBody = extractRequestBody(attrs);
        if (reqBody != null) {
            log.info("[http] >> body={}", reqBody);
        }

        try {
            Object result = joinPoint.proceed();
            // 打印响应体
            String respBody = extractResponseBody(attrs);
            if (respBody != null) {
                log.info("[http] << {}", respBody);
            } else {
                log.info("[http] << (no body)");
            }
            return result;
        } catch (Throwable ex) {
            // 异常时响应体尚未写入（由后续 HandlerExceptionResolver 处理）
            log.info("[http] << ERROR: {}", ex.toString());
            throw ex;
        }
    }

    // ========== 请求体 ==========

    private String extractRequestBody(ServletRequestAttributes attrs) {
        if (attrs == null) return null;
        HttpServletRequest request = attrs.getRequest();
        String method = request.getMethod();
        if ("GET".equalsIgnoreCase(method) || "DELETE".equalsIgnoreCase(method)) {
            return null;
        }
        ContentCachingRequestWrapper wrapper =
                WebUtils.getNativeRequest(request, ContentCachingRequestWrapper.class);
        if (wrapper == null) return null;
        byte[] buf = wrapper.getContentAsByteArray();
        if (buf == null || buf.length == 0) return null;
        return truncateBody(new String(buf, StandardCharsets.UTF_8));
    }

    // ========== 响应体 ==========

    private String extractResponseBody(ServletRequestAttributes attrs) {
        if (attrs == null) return null;
        HttpServletResponse response = attrs.getResponse();
        if (response == null) return null;
        ContentCachingResponseWrapper wrapper =
                WebUtils.getNativeResponse(response, ContentCachingResponseWrapper.class);
        if (wrapper == null) return null;
        byte[] buf = wrapper.getContentAsByteArray();
        if (buf == null || buf.length == 0) return null;
        return truncateBody(new String(buf, StandardCharsets.UTF_8));
    }

    private String truncateBody(String raw) {
        raw = raw.trim().replaceAll("\\s+", " ");
        if (raw.length() > BODY_MAX_LEN) {
            return raw.substring(0, BODY_MAX_LEN) + "...";
        }
        return raw;
    }
}
