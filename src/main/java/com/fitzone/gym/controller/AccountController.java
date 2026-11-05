package com.fitzone.gym.controller;

import com.fitzone.gym.model.User;
import com.fitzone.gym.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AccountController {

    private final UserRepository users;
    private final PasswordEncoder encoder;

    // ---------- Owner profile ----------

    @GetMapping("/owner/profile")
    public String profile(Authentication auth, Model model) {
        model.addAttribute("owner", me(auth));
        return "owner_profile";
    }

    @PostMapping("/owner/profile")
    public String saveProfile(Authentication auth,
                              @RequestParam(defaultValue = "") String name,
                              @RequestParam(defaultValue = "") String email,
                              @RequestParam(defaultValue = "") String mobile,
                              RedirectAttributes ra) {
        User u = me(auth);
        String n = name.trim();
        String e = email.trim().toLowerCase();
        String m = mobile.trim();

        String error = null;
        if (n.isEmpty() || n.length() > 100)
            error = "Naam zaroori hai";
        else if (e.length() > 120 || !e.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$"))
            error = "Sahi email daalein";
        else if (users.existsByEmailAndIdNot(e, u.getId()))
            error = "Ye email kisi aur account ka hai";
        else if (!m.isEmpty() && !m.matches("[6-9]\\d{9}"))
            error = "Sahi 10 digit mobile number daalein";
        else if (!m.isEmpty() && users.existsByMobileAndIdNot(m, u.getId()))
            error = "Ye mobile kisi aur account ka hai";

        if (error != null) {
            ra.addFlashAttribute("error", error);
            return "redirect:/owner/profile";
        }

        u.setName(n);
        u.setEmail(e);
        u.setMobile(m.isEmpty() ? null : m);
        users.save(u);

        ra.addFlashAttribute("success", "Profile update ho gayi");
        return "redirect:/owner/profile";
    }

    // ---------- Change password (owner aur member dono) ----------

    @GetMapping("/account/password")
    public String passwordForm() {
        return "change_password";
    }

    @PostMapping("/account/password")
    public String changePassword(Authentication auth,
                                 @RequestParam(defaultValue = "") String currentPassword,
                                 @RequestParam(defaultValue = "") String newPassword,
                                 @RequestParam(defaultValue = "") String confirmPassword,
                                 RedirectAttributes ra) {
        User u = me(auth);

        String error = null;
        if (!encoder.matches(currentPassword, u.getPasswordHash()))
            error = "Abhi ka password galat hai";
        else if (newPassword.length() < 8)
            error = "Naya password kam se kam 8 character ka ho";
        else if (newPassword.length() > 72)
            error = "Password 72 character se chhota ho";
        else if (!newPassword.equals(confirmPassword))
            error = "Dono naye password match nahi karte";
        else if (newPassword.equals(currentPassword))
            error = "Naya password purane se alag ho";

        if (error != null) {
            ra.addFlashAttribute("error", error);
            return "redirect:/account/password";
        }

        u.setPasswordHash(encoder.encode(newPassword));
        users.save(u);

        ra.addFlashAttribute("success", "Password badal gaya");
        return "redirect:/account/password";
    }

    private User me(Authentication auth) {
        // ID login se aati hai, URL se nahi, isliye koi dusre ka account nahi badal sakta
        return users.findById(Long.parseLong(auth.getName())).orElseThrow();
    }
}