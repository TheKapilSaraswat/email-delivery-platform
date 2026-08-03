package com.emailplatform.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Fixed-window in-memory rate limiter for /api/** endpoints.
 *
 * Configured via:
 *   app.rate-limit.enabled        (default true)
 *   app.rate-limit.max-requests   (default 300 requests per window)
 *   app.rate-limit.window-seconds (default 60 seconds)
 *
 * Health, readiness and Swagger endpoints are always exempt.
 * Clients are keyed by the first X-Forwarded-For value when present
 * (important behind Render/Railway proxies) or the remote address.
 */
@Component
@Order(-200)
public class RateLimitFilter extends OncePerRequestFilter {

    @Value("${app.rate-limit.enabled:true}")
    private boolean enabled;

    @Value("${app.rate-limit.max-requests:300}")
    private int maxRequests;

    @Value("${app.rate-limit.window-seconds:60}")
    private int windowSeconds;

    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (!enabled) {
            return true;
        }
        String path = request.getRequestURI();
        if (path.startsWith("/api/health") || path.startsWith("/api/ready")) {
            return true;
        }
        if (path.startsWith("/swagger") || path.startsWith("/v3/api-docs")) {
            return true;
        }
        return !path.startsWith("/api/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String key = clientKey(request);
        long now = System.currentTimeMillis();
        Window window = windows.compute(key, (k, existing) -> {
            if (existing == null || now - existing.startedAt > windowSeconds * 1000L) {
                return new Window(now);
            }
            return existing;
        });

        if (window.count.incrementAndGet() > maxRequests) {
            response.setStatus(HttpServletResponse.SC_TOO_MANY_REQUESTS);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Retry-After", String.valueOf(windowSeconds));
            response.getWriter().write("{\"error\":\"Too many requests. Please try again later.\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String clientKey(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        String remote = request.getRemoteAddr();
        return remote == null ? "unknown" : remote;
    }

    private static final class Window {
        final long startedAt;
        final AtomicInteger count = new AtomicInteger();

        Window(long startedAt) {
            this.startedAt = startedAt;
        }
    }
}
