package com.fitzone.gym.repository;

import com.fitzone.gym.model.GymSettings;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GymSettingsRepository extends JpaRepository<GymSettings, Long> {
}