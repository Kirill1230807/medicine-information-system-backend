package com.bank.userservice.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerMapping;

import java.io.IOException;
import java.time.Instant;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ObservabilityFilter extends OncePerRequestFilter {

    private static final Set<String> SKIP_PATHS = Set.of("/actuator/prometheus", "/actuator/health");

    @Value("${app.fail-rate:0.0}")
    private double failRate;

    @Value("${app.version:dev}")
    private String appVersion;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        if (SKIP_PATHS.contains(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        long startNs = System.nanoTime();
        try {
            if (failRate > 0 && ThreadLocalRandom.current().nextDouble() < failRate) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"injected failure\"}");
                return;
            }
            filterChain.doFilter(request, response);
        } finally {
            double durationMs = Math.round((System.nanoTime() - startNs) / 100_000.0) / 10.0;
            Object pattern = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
            String route = pattern != null ? pattern.toString() : "unknown";
            int status = response.getStatus();
            String level = status >= 500 ? "error" : "info";

            String jsonLog = String.format(
                    java.util.Locale.US,
                    "{\"ts\":\"%s\",\"level\":\"%s\",\"msg\":\"http_request\",\"method\":\"%s\",\"route\":\"%s\",\"status\":%d,\"duration_ms\":%.1f,\"version\":\"%s\"}",
                    Instant.now(),
                    level,
                    escapeJson(request.getMethod()),
                    escapeJson(route),
                    status,
                    durationMs,
                    escapeJson(appVersion)
            );

            System.out.println(jsonLog);
        }
    }

    private String escapeJson(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}