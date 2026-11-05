package com.fitzone.gym.config;

import com.fitzone.gym.model.Plan;
import com.fitzone.gym.model.User;
import com.fitzone.gym.repository.PlanRepository;
import com.fitzone.gym.repository.UserRepository;
import com.fitzone.gym.service.GymSettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final PlanRepository plans;
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final GymSettingsService gymSettings;

    @Value("${gym.owner.email}")
    private String ownerEmail;

    @Value("${gym.owner.password}")
    private String ownerPassword;

    // Password bhool jao to ek baar true karke restart karo, phir wapas false
    @Value("${gym.owner.reset-on-start:false}")
    private boolean resetOnStart;

    @Override
    public void run(String... args) {
        gymSettings.get();       // pehli baar default settings ban jati hain

        if (plans.count() == 0) {
            plans.saveAll(List.of(
                    plan("1 Month", 1, 800),
                    plan("3 Months", 3, 2100),
                    plan("6 Months", 6, 3800),
                    plan("1 Year", 12, 6500)));
        }

        String email = ownerEmail.trim().toLowerCase();
        String password = ownerPassword.trim();

        User owner = users.findByRole("OWNER").stream().findFirst().orElse(null);
        if (owner == null) {
            owner = new User();
            owner.setRole("OWNER");
            owner.setName("Gym Owner");
            owner.setEmail(email);
            owner.setPasswordHash(encoder.encode(password));
            users.save(owner);
            System.out.println(">>> Owner account ban gaya: " + email);
        } else if (resetOnStart) {
            owner.setEmail(email);
            owner.setPasswordHash(encoder.encode(password));
            users.save(owner);
            System.out.println(">>> Owner reset hua: " + email);
        }
    }

    private Plan plan(String name, int months, int price) {
        Plan p = new Plan();
        p.setName(name);
        p.setMonths(months);
        p.setPrice(price);
        return p;
    }
}