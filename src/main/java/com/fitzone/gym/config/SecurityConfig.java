package com.fitzone.gym.config;

import com.fitzone.gym.service.LoginAttemptService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;

@Configuration
public class SecurityConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();      // password hashing
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http, LoginAttemptService attempts) throws Exception {
        HttpSessionRequestCache cache = new HttpSessionRequestCache();

        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/about", "/contact", "/owner/login", "/member/login",
                                 "/css/**", "/js/**", "/images/**", "/error").permitAll()
                .requestMatchers("/owner/**").hasRole("OWNER")     // sirf owner
                .requestMatchers("/member/**").hasRole("MEMBER")   // sirf member
                .anyRequest().authenticated())
            .formLogin(form -> form
                .loginPage("/member/login")
                .loginProcessingUrl("/login")
                .successHandler((req, res, authentication) -> {
                    attempts.succeeded(req.getParameter("username"));

                    boolean owner = authentication.getAuthorities().stream()
                            .anyMatch(a -> a.getAuthority().equals("ROLE_OWNER"));

                    // QR scan se aaya tha to login ke baad wahin wapas bhejo
                    SavedRequest saved = cache.getRequest(req, res);
                    if (owner && saved != null && saved.getRedirectUrl().contains("/owner/verify/")) {
                        cache.removeRequest(req, res);
                        res.sendRedirect(saved.getRedirectUrl());
                        return;
                    }
                    res.sendRedirect(owner ? "/owner/dashboard" : "/member/dashboard");
                })
                .failureHandler((req, res, ex) -> {
                    String page = "owner".equals(req.getParameter("from"))
                            ? "/owner/login" : "/member/login";

                    if (ex instanceof LockedException) {
                        res.sendRedirect(page + "?locked");
                        return;
                    }
                    if (ex instanceof BadCredentialsException) {
                        attempts.failed(req.getParameter("username"));
                    }
                    res.sendRedirect(page + "?error");
                }))
            .exceptionHandling(e -> e.authenticationEntryPoint((req, res, ex) -> {
                if (req.getRequestURI().startsWith("/owner/verify/")) {
                    cache.saveRequest(req, res);
                }
                String page = req.getRequestURI().startsWith("/owner")
                        ? "/owner/login" : "/member/login";
                res.sendRedirect(page);
            }))
            .logout(l -> l.logoutUrl("/logout").logoutSuccessUrl("/"));

        return http.build();
    }
}