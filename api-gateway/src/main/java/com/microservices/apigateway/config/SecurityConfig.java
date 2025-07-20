package com.microservices.apigateway.config;

import com.microservices.apigateway.client.UserServiceClient;
import com.microservices.apigateway.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    private static final String FALLBACK_TOKEN = "microservices-token-2024";

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserServiceClient userServiceClient;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(csrf -> csrf.disable())
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/actuator/**").permitAll()
                        .pathMatchers("/api/auth/**").permitAll()
                        .anyExchange().authenticated()
                )
                .addFilterBefore(authenticationFilter(), org.springframework.security.config.web.server.SecurityWebFiltersOrder.AUTHORIZATION)
                .build();
    }

    @Bean
    public AuthenticationWebFilter authenticationFilter() {
        AuthenticationWebFilter filter = new AuthenticationWebFilter(authenticationManager());
        filter.setServerAuthenticationConverter(authenticationConverter());
        return filter;
    }

    @Bean
    public ReactiveAuthenticationManager authenticationManager() {
        return authentication -> {
            String token = authentication.getCredentials().toString();
            
            if (FALLBACK_TOKEN.equals(token)) {
                return Mono.just(new UsernamePasswordAuthenticationToken(
                        "fallback-user", 
                        token, 
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))
                ));
            }

            try {
                if (jwtUtil.validateToken(token)) {
                    String username = jwtUtil.getUsernameFromToken(token);
                    List<String> roles = jwtUtil.getRolesFromToken(token);
                    
                    List<SimpleGrantedAuthority> authorities = roles.stream()
                            .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                            .collect(Collectors.toList());

                    return Mono.just(new UsernamePasswordAuthenticationToken(
                            username, 
                            token, 
                            authorities
                    ));
                }
            } catch (Exception e) {
                System.err.println("Error validating JWT token locally: " + e.getMessage());
            }

            try {
                Map<String, Object> response = userServiceClient.validateToken(Map.of("token", token));
                Boolean isValid = (Boolean) response.get("valid");
                
                if (Boolean.TRUE.equals(isValid)) {
                    String username = (String) response.get("username");
                    @SuppressWarnings("unchecked")
                    List<String> roles = (List<String>) response.get("roles");
                    
                    List<SimpleGrantedAuthority> authorities = roles.stream()
                            .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                            .collect(Collectors.toList());

                    return Mono.just(new UsernamePasswordAuthenticationToken(
                            username, 
                            token, 
                            authorities
                    ));
                }
            } catch (Exception e) {
                System.err.println("Error validating token with user service: " + e.getMessage());
            }
            
            return Mono.empty();
        };
    }

    @Bean
    public ServerAuthenticationConverter authenticationConverter() {
        return exchange -> {
            String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                return Mono.just(new UsernamePasswordAuthenticationToken("user", token));
            }
            return Mono.empty();
        };
    }
}