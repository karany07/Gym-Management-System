package com.fitzone.gym.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "gym_settings")
@Getter @Setter
public class GymSettings {
    @Id
    private Long id = 1L;            // hamesha sirf ek row

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 300)
    private String address;

    @Column(length = 20)
    private String phone;

    @Column(length = 120)
    private String email;

    @Column(length = 1000)
    private String about;
}