package com.example.alquila_seguro_backend.security.service;

import com.example.alquila_seguro_backend.entity.Admin;
import com.example.alquila_seguro_backend.repositories.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.logging.Logger;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    private final AdminRepository adminRepository;
    private final static Logger logger =  Logger.getLogger(UserDetailsServiceImpl.class.getName());

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.info("Autenticando al usuario: {} "+ username);

        Admin admin = adminRepository.findByUsername(username)
                .orElseThrow(()->
                        new UsernameNotFoundException("Usuario con el nombre " + username + " no existe"));

        return UserDetailsImpl.build(admin);
    }
}
