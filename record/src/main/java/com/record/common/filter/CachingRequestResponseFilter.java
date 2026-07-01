package com.record.common.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.util.UUID;

/**
 * 缓存请求体和响应体的过滤器。
 * <p>用 {@link ContentCachingRequestWrapper} 和 {@link ContentCachingResponseWrapper}
 * 在最前置位置包裹，使切面在请求处理完毕后仍能读取请求体和响应体内容用于日志。</p>
 * <p>同时为每个请求生成 trace ID（{@code rid}）写入 MDC，
 * 后续所有日志（业务日志、SQL 日志等）都会带上该 ID，实现全链路追踪。</p>
 * <p>注意：{@code ContentCachingResponseWrapper} 不会自动将缓存内容写回原始响应，
 * 必须在 {@code chain.doFilter()} 之后调用 {@code copyBodyToResponse()}。</p>
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CachingRequestResponseFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        // 生成全链路追踪 ID（16 位十六进制，无横线）
        String rid = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        MDC.put("rid", rid);
        try {
            chain.doFilter(requestWrapper, responseWrapper);
        } finally {
            MDC.remove("rid");
            responseWrapper.copyBodyToResponse();
        }
    }
}
