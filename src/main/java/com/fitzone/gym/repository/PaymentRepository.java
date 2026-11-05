package com.fitzone.gym.repository;

import com.fitzone.gym.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findAllByOrderByPaymentDateDescIdDesc();
    List<Payment> findByMemberIdOrderByPaymentDateDescIdDesc(Long memberId);
}