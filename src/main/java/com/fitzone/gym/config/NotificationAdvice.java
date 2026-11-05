package com.fitzone.gym.config;

import com.fitzone.gym.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class NotificationAdvice {

    private final NotificationRepository notifications;

    @ModelAttribute("unreadCount")
    public long unreadCount(Authentication auth) {
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken)
            return 0;
        try {
            return notifications.countByRecipientIdAndSeenFalse(Long.parseLong(auth.getName()));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}