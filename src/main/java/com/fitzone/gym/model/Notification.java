package com.fitzone.gym.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter @Setter
public class Notification {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "recipient_id")
    private User recipient;

    @Column(nullable = false, length = 20)
    private String severity;          // info, success, warning, danger

    @Column(nullable = false, length = 300)
    private String message;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private boolean seen;

    // Ek hi alert dobara na jaye, isliye har alert ka ek unique key
    @Column(name = "dedupe_key", length = 80)
    private String dedupeKey;
}