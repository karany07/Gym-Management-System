package com.fitzone.gym.service;

import com.fitzone.gym.model.GymSettings;
import com.fitzone.gym.repository.GymSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GymSettingsService {

    private final GymSettingsRepository repo;

    // Pehli baar ke default, application.properties se
    @Value("${gym.name}")
    private String defaultName;

    @Value("${gym.phone:}")
    private String defaultPhone;

    @Value("${gym.address:}")
    private String defaultAddress;

    @Value("${gym.email:}")
    private String defaultEmail;

    public GymSettings get() {
        return repo.findById(1L).orElseGet(() -> {
            GymSettings g = new GymSettings();
            g.setName(defaultName);
            g.setPhone(defaultPhone);
            g.setAddress(defaultAddress);
            g.setEmail(defaultEmail);
            g.setAbout("");
            return repo.save(g);
        });
    }

    /** Sahi ho to null, warna error message. */
    public String update(String name, String address, String phone, String email, String about) {
        name = clean(name);
        address = clean(address);
        phone = clean(phone);
        email = clean(email);
        about = clean(about);

        if (name.isEmpty() || name.length() > 100)
            return "Gym ka naam zaroori hai (100 akshar tak)";
        if (address.length() > 300)
            return "Address 300 akshar se chhota ho";
        if (!phone.isEmpty() && !phone.matches("[0-9+\\- ]{7,20}"))
            return "Sahi phone number daalein";
        if (!email.isEmpty() && !email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$"))
            return "Sahi email daalein";
        if (about.length() > 1000)
            return "About 1000 akshar se chhota ho";

        GymSettings g = get();
        g.setName(name);
        g.setAddress(address);
        g.setPhone(phone);
        g.setEmail(email);
        g.setAbout(about);
        repo.save(g);
        return null;
    }

    private String clean(String s) {
        return s == null ? "" : s.trim();
    }
}