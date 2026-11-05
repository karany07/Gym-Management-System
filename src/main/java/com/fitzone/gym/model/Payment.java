package com.fitzone.gym.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "payments")
@Getter @Setter
public class Payment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Member member;

    @Column(nullable = false)
    private String planName;          // us waqt ka plan ka naam, plan badle to bhi history same rahe

    @Column(nullable = false)
    private int amount;

    @Column(nullable = false)
    private LocalDate paymentDate;

    @Column(nullable = false, length = 20)
    private String method;

    @Column(length = 60)
    private String transactionId;

    @Column(nullable = false)
    private LocalDate nextExpiry;
}