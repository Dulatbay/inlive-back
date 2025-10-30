package ai.lab.inlive.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
public class CustomCorsFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    @NotNull FilterChain filterChain) throws ServletException, IOException {

        String origin = request.getHeader("Origin");

        if (origin != null && (
                origin.startsWith("http://localhost:") ||
                        origin.startsWith("https://localhost:") ||
                        origin.startsWith("http://127.0.0.1:") ||
                        origin.startsWith("https://127.0.0.1:") ||
                        origin.equals("http://10.36.40.16:3000") ||
                        origin.endsWith("your-production-domain.com")
        )) {
            response.setHeader("Access-Control-Allow-Origin", origin);
            response.setHeader("Vary", "Origin");
        }

        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, PATCH");

        String reqHeaders = request.getHeader("Access-Control-Request-Headers");
        if (reqHeaders != null && !reqHeaders.isBlank()) {
            response.setHeader("Access-Control-Allow-Headers", reqHeaders);
        } else {
            response.setHeader("Access-Control-Allow-Headers",
                    "Authorization, Content-Type, X-Requested-With, Accept, Origin, X-XSRF-TOKEN, X-CSRF-TOKEN");
        }

        response.setHeader("Access-Control-Expose-Headers", "Authorization, XSRF-TOKEN");

        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Max-Age", "3600");

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        filterChain.doFilter(request, response);
    }
}