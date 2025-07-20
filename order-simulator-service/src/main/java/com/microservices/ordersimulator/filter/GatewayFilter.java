package com.microservices.ordersimulator.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class GatewayFilter implements Filter {

    private static final String GATEWAY_TOKEN = "microservices-internal-2024";
    private static final String GATEWAY_HEADER = "X-Gateway-Token";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String requestURI = httpRequest.getRequestURI();
        
        // Allow actuator endpoints for health checks
        if (requestURI.startsWith("/actuator")) {
            chain.doFilter(request, response);
            return;
        }
        
        // Check for gateway token
        String gatewayToken = httpRequest.getHeader(GATEWAY_HEADER);
        
        if (!GATEWAY_TOKEN.equals(gatewayToken)) {
            httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
            httpResponse.setContentType("application/json");
            httpResponse.getWriter().write("{\"error\":\"Direct access not allowed. Use API Gateway.\"}");
            return;
        }
        
        chain.doFilter(request, response);
    }
}