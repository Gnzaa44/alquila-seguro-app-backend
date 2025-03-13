package com.example.alquila_seguro_backend.security.service;

import com.example.alquila_seguro_backend.entity.Admin;
import com.example.alquila_seguro_backend.repositories.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    private final AdminRepository adminRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Admin admin = adminRepository.findByUsername(username)
                .orElseThrow(()-> new UsernameNotFoundException("Usuario con el nombre " + username + " no existe"));

        return new User(admin.getUsername(), admin.getPassword(), Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")));

    }
}
