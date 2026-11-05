package com.fitzone.gym.controller;

import com.fitzone.gym.repository.NotificationRepository;
import com.fitzone.gym.service.ExpiryCheckService;
import com.fitzone.gym.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRepository notifications;
    private final NotificationService service;
    private final ExpiryCheckService expiryCheck;

    @GetMapping("/notifications")
    public String list(Authentication auth, Model model) {
        Long userId = Long.parseLong(auth.getName());
        model.addAttribute("items",
                notifications.findTop100ByRecipientIdOrderByCreatedAtDescIdDesc(userId));
        return "notifications";
    }

    @PostMapping("/notifications/read-all")
    public String readAll(Authentication auth) {
        service.markAllRead(Long.parseLong(auth.getName()));
        return "redirect:/notifications";
    }

    // Sirf owner (/owner/** par security rule laga hai). Test ke liye kaam ka.
    @PostMapping("/owner/notifications/check")
    public String runCheck(RedirectAttributes ra) {
        int created = expiryCheck.runCheck();
        ra.addFlashAttribute("success", "Check complete. " + created + " nayi notifications bani.");
        return "redirect:/notifications";
    }
}