package com.kusitms.socketservice.global.config.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kusitms.socketservice.global.error.dto.ErrorBaseResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static com.kusitms.socketservice.global.error.ErrorCode.UNAUTHORIZED;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        handleException(response);
    }

    private void handleException(HttpServletResponse response) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("utf-8");
        response.setStatus(UNAUTHORIZED.getHttpStatus().value());
        response.getWriter().write(objectMapper.writeValueAsString(ErrorBaseResponse.of(UNAUTHORIZED)));
    }
}