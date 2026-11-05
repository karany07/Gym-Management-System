package com.fitzone.gym.controller;

import com.fitzone.gym.service.GymSettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/owner/settings")
@RequiredArgsConstructor
public class SettingsController {

    private final GymSettingsService settings;

    @GetMapping
    public String form() {
        return "settings";           // "gym" attribute advice se mil jata hai
    }

    @PostMapping
    public String save(@RequestParam(defaultValue = "") String name,
                       @RequestParam(defaultValue = "") String address,
                       @RequestParam(defaultValue = "") String phone,
                       @RequestParam(defaultValue = "") String email,
                       @RequestParam(defaultValue = "") String about,
                       RedirectAttributes ra) {
        String error = settings.update(name, address, phone, email, about);
        if (error != null) ra.addFlashAttribute("error", error);
        else ra.addFlashAttribute("success", "Gym settings save ho gayi");
        return "redirect:/owner/settings";
    }
}