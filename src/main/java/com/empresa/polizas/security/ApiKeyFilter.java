package com.empresa.polizas.security;

import com.empresa.polizas.config.ApiKeyProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;

@Component
public class ApiKeyFilter extends OncePerRequestFilter {

    static final String HEADER = "x-api-key";
    private static final String MENSAJE = "API Key ausente o inválida";

    private final ApiKeyProperties properties;

    public ApiKeyFilter(ApiKeyProperties properties) {
        this.properties = properties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String recibida = request.getHeader(HEADER);
        if (recibida != null && coincide(recibida)) {
            chain.doFilter(request, response);
            return;
        }
        responderNoAutorizado(response);
    }

    private boolean coincide(String recibida) {
        return MessageDigest.isEqual(
                recibida.getBytes(StandardCharsets.UTF_8),
                properties.apiKey().getBytes(StandardCharsets.UTF_8));
    }

    private void responderNoAutorizado(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(String.format(
                "{\"timestamp\":\"%s\",\"status\":401,\"error\":\"Unauthorized\","
                        + "\"mensaje\":\"%s\",\"detalles\":[]}",
                LocalDateTime.now(), MENSAJE));
    }
}