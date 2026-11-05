package com.fitzone.gym.repository;

import com.fitzone.gym.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findTop100ByRecipientIdOrderByCreatedAtDescIdDesc(Long recipientId);
    long countByRecipientIdAndSeenFalse(Long recipientId);
    boolean existsByRecipientIdAndDedupeKey(Long recipientId, String dedupeKey);
    List<Notification> findByRecipientIdAndSeenFalse(Long recipientId);
}