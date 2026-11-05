package com.fitzone.gym.service;

import com.fitzone.gym.model.Notification;
import com.fitzone.gym.model.User;
import com.fitzone.gym.repository.NotificationRepository;
import com.fitzone.gym.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository repo;
    private final UserRepository users;

    /** Ek user ko notification bhejo. Nayi bani to 1, pehle se thi (dedupeKey same) to 0. */
    public int send(User to, String severity, String message, String dedupeKey) {
        if (dedupeKey != null && repo.existsByRecipientIdAndDedupeKey(to.getId(), dedupeKey))
            return 0;

        Notification n = new Notification();
        n.setRecipient(to);
        n.setSeverity(severity);
        n.setMessage(message);
        n.setCreatedAt(LocalDateTime.now());
        n.setDedupeKey(dedupeKey);
        repo.save(n);
        return 1;
    }

    /** Sabhi owners ko bhejo. Kitni nayi bani wo wapas deta hai. */
    public int sendToOwners(String severity, String message, String dedupeKey) {
        int created = 0;
        for (User owner : users.findByRole("OWNER")) {
            created += send(owner, severity, message, dedupeKey);
        }
        return created;
    }

    @Transactional
    public void markAllRead(Long userId) {
        List<Notification> unread = repo.findByRecipientIdAndSeenFalse(userId);
        unread.forEach(n -> n.setSeen(true));
        repo.saveAll(unread);
    }
}