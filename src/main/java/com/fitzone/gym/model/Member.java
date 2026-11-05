package com.fitzone.gym.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "members")
@Getter @Setter
public class Member {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    @ManyToOne(optional = false)
    private Plan plan;

    private LocalDate dob;
    private String gender;

    private LocalDate joinDate;            // pehli baar judne ki date

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate expiryDate;

    private int amountPaid;
    private String photo;

    @Column(unique = true, length = 40)
    private String qrToken;                // QR me yahi random token jata hai

    // Database me save nahi hote, har baar aaj ki date se calculate hote hain
    public long getDaysLeft() {
        return ChronoUnit.DAYS.between(LocalDate.now(), expiryDate);
    }

    public String getStatus() {
        long d = getDaysLeft();
        if (d <= -2) return "archived";
        if (d <= 0)  return "expired";
        if (d <= 7)  return "expiring";
        return "active";
    }
}