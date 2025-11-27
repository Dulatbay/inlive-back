package ai.lab.inlive.security;

import ai.lab.inlive.dto.response.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;

public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final MessageSource messageSource;

    public CustomAccessDeniedHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException {
        response.setContentType("application/json; charset=utf-8");
        response.setStatus(HttpStatus.FORBIDDEN.value());

        String errorMessage = messageSource.getMessage("error.forbidden", null, LocaleContextHolder.getLocale());
        String accessDeniedMessage = messageSource.getMessage("error.auth.accessDenied", 
                new Object[]{request.getRequestURI()}, LocaleContextHolder.getLocale());

        response.getOutputStream().println(objectMapper.writeValueAsString(
                new ErrorResponse(errorMessage,
                        accessDeniedMessage,
                        ""
                )));
    }
}