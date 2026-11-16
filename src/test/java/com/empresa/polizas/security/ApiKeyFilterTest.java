package com.empresa.polizas.security;

import com.empresa.polizas.config.ApiKeyProperties;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApiKeyFilterTest {

    private final ApiKeyFilter filter = new ApiKeyFilter(new ApiKeyProperties("123456"));

    @Test
    void sinHeaderResponde401YNoContinuaLaCadena() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/polizas");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertEquals(401, response.getStatus());
        assertTrue(response.getContentAsString().contains("\"status\":401"));
        assertNull(chain.getRequest());
    }

    @Test
    void conApiKeyIncorrectaResponde401() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/polizas");
        request.addHeader("x-api-key", "incorrecta");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertEquals(401, response.getStatus());
        assertNull(chain.getRequest());
    }

    @Test
    void conApiKeyCorrectaContinuaLaCadena() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/polizas");
        request.addHeader("x-api-key", "123456");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertEquals(200, response.getStatus());
        assertNotNull(chain.getRequest());
    }

    @Test
    void laConsolaH2NoSeFiltra() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/h2-console/login.do");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertNotNull(chain.getRequest());
    }
}