package com.fitzone.gym.config;

import com.fitzone.gym.model.GymSettings;
import com.fitzone.gym.service.GymSettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class GymInfoAdvice {

    private final GymSettingsService settings;

    @ModelAttribute
    public void addGymInfo(Model model) {
        GymSettings g = settings.get();
        model.addAttribute("gym", g);
        model.addAttribute("gymName", g.getName());
        model.addAttribute("gymPhone", g.getPhone());
        model.addAttribute("gymAddress", g.getAddress());
    }
}