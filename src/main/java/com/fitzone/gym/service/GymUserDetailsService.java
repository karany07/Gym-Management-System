package com.fitzone.gym.service;

import com.fitzone.gym.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GymUserDetailsService implements UserDetailsService {

    private final UserRepository users;
    private final LoginAttemptService attempts;

    @Override
    public UserDetails loadUserByUsername(String input) throws UsernameNotFoundException {
        String id = input.trim();
        com.fitzone.gym.model.User u = users
                .findByEmailOrMobileOrMemberCode(id.toLowerCase(), id, id.toUpperCase())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return org.springframework.security.core.userdetails.User
                .withUsername(String.valueOf(u.getId()))
                .password(u.getPasswordHash())
                .roles(u.getRole())                       // ROLE_OWNER / ROLE_MEMBER
                .accountLocked(attempts.isLocked(id))     // 5 galat koshish ke baad lock
                .build();
    }
}