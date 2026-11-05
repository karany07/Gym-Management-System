package com.fitzone.gym.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Getter @Setter
public class MemberForm {
    private String fullName;
    private String mobile;
    private String email;
    private String gender;
    private String password;
    private Long planId;
    private Integer amount;
    private String paymentMethod;
    private String transactionId;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dob;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;

    private MultipartFile photo;
}