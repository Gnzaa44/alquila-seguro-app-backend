package com.example.alquila_seguro_backend.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthFilter.class);

    private final JwtUtils jwtUtils;
    private final UserDetailsService userDetailsService;
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    // Lista de rutas que no deben ser procesadas por este filtro
    private static final List<String> excludedPaths = Arrays.asList(
            "/alquila-seg/auth/**",
            "/alquila-seg/clients/**",
            "/alquila-seg/reservations",
            "/alquila-seg/consultancies",
            "/alquila-seg/payments/webhooks",
            "/alquila-seg/contracts/**",
            "/alquila-seg/invoices/**",
            "/alquila-seg/auth/login",
            "/alquila-seg/payments/reservations/*/create-preference",
            "/alquila-seg/payments/consultancies/*/create-preference",// Adaptado para variable de ruta
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String path = request.getServletPath();
        boolean shouldProcess = true;

        for (String excludedPath : excludedPaths) {
            if (antPathMatcher.match(excludedPath, path)) {
                shouldProcess = false;
                break;
            }
        }

        if (shouldProcess) {
            try {
                String jwt = jwtUtils.parseJwt(request);
                if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
                    String username = jwtUtils.getUserNameFromJwtToken(jwt);
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (Exception e) {
                logger.error("No se puede autenticar al usuario: {} ", e.getMessage());
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }

}
