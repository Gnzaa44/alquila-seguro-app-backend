package com.example.alquila_seguro_backend.controller;

import com.example.alquila_seguro_backend.dto.ApiResponse;
import com.example.alquila_seguro_backend.dto.JwtResponse;
import com.example.alquila_seguro_backend.dto.LoginRequest;
import com.example.alquila_seguro_backend.security.jwt.JwtUtils;
import com.example.alquila_seguro_backend.security.service.UserDetailsImpl;
import com.example.alquila_seguro_backend.security.service.UserDetailsServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequestMapping("/alquila-seg/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    @PostMapping("/login")
    public ResponseEntity<?> authenticate(@Valid @RequestBody LoginRequest loginRequest) {

        try {
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(),
                    loginRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateJwtToken(authentication);
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            return ResponseEntity.ok(ApiResponse.<JwtResponse>builder()
                    .success(true)
                    .message("Autenticación exitosa")
                    .data(JwtResponse.builder()
                            .token(jwt)
                            .id(userDetails.getId())
                            .username(userDetails.getUsername())
                            .role("ADMIN")
                            .build())
                    .build());
        } catch (BadCredentialsException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales incorrectas.");
        }catch(AuthenticationException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Autenticacion fallida.");
        }catch(Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor.");
        }
    }

}
