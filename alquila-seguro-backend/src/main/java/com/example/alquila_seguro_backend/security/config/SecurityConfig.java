package com.example.alquila_seguro_backend.security.config;

import com.example.alquila_seguro_backend.security.jwt.JwtAuthEntryPoint;
import com.example.alquila_seguro_backend.security.jwt.JwtAuthFilter;
import com.example.alquila_seguro_backend.security.service.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final UserDetailsServiceImpl userDetailsService;
    private final JwtAuthEntryPoint jwtAuthEntryPoint;
    private final JwtAuthFilter jwtAuthFilter;
    
    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setUserDetailsService(userDetailsService);
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
        return daoAuthenticationProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // IMPORTANT: Cambia estos orígenes a los dominios de tu frontend
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000",
                "http://localhost:3001",
                "http://localhost:3002",
                "http://localhost:3003",
                "https://alquilaseguro.com.ar",
                "http://localhost:5173"

        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*")); // Permite todas las cabeceras
        configuration.setAllowCredentials(true); // Permite enviar cookies o cabeceras de autenticación (JWT)
        configuration.setMaxAge(3600L); // Duración del pre-flight request en segundos

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // Aplica esta configuración a todos los endpoints
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .exceptionHandling(exception -> exception.authenticationEntryPoint(jwtAuthEntryPoint))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.PUT, "/alquila-seg/reservations/*/confirm").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/alquila-seg/reservations/*/cancel").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/alquila-seg/reservations/*/complete").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/alquila-seg/properties/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/alquila-seg/properties/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/alquila-seg/properties").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/alquila-seg/consultancies/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/alquila-seg/auth/login").permitAll()
                        .requestMatchers(HttpMethod.GET, "/alquila-seg/properties/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/alquila-seg/contracts/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/alquila-seg/invoices/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/alquila-seg/reservations/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/alquila-seg/consultancies/**").permitAll()
                        .requestMatchers("/alquila-seg/payments/webhooks").permitAll()
                        .requestMatchers("/alquila-seg/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/alquila-seg/clients/**").permitAll()
                        .requestMatchers(HttpMethod.POST,"/alquila-seg/reservations").permitAll()
                        .requestMatchers(HttpMethod.POST,"/alquila-seg/consultancies").permitAll()
                        .requestMatchers("/alquila-seg/payments/reservations/*/create-preference").permitAll()
                        .requestMatchers("/alquila-seg/payments/consultancies/*/create-preference").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .anyRequest().authenticated()
                );
        http.authenticationProvider(daoAuthenticationProvider());
        http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

}
