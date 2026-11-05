package com.fitzone.gym.model;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter @Setter

public class User {
    
    @Id @GeneratedValue (strategy = GenerationType. IDENTITY)
    private  Long id;

    @Column (nullable = false, length = 10)
    private String role;          // "Owner " ya "Member"

    @Column (nullable = false, length = 100)
    private  String name;

    @Column (unique = true)
    private String email;

    @Column (unique = true, length = 15)
    private String mobile;

    @Column  (unique = true, length = 20)
    private String memberCode;         //gym001

    @Column (nullable = false)
    private String passwordHash;
 }
