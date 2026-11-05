package com.fitzone.gym.service;

import com.fitzone.gym.model.Member;
import com.fitzone.gym.model.Payment;
import com.fitzone.gym.model.User;
import com.fitzone.gym.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService {

    public static final List<String> METHODS = List.of("Cash", "UPI", "Card", "Bank Transfer");

    private final PaymentRepository payments;
    private final NotificationService notifications;

    /** Sahi ho to null, warna error message. */
    public static String validate(String method, String transactionId) {
        if (method == null || !METHODS.contains(method))
            return "Payment method chuniye";
        if (transactionId != null && transactionId.trim().length() > 60)
            return "Transaction ID 60 akshar se chhota ho";
        return null;
    }

    /** Member ko save karne ke BAAD call karo, taaki nayi expiry record me aaye. */
    public void record(Member member, int amount, String method, String transactionId) {
        Payment p = new Payment();
        p.setMember(member);
        p.setPlanName(member.getPlan().getName());
        p.setAmount(amount);
        p.setPaymentDate(LocalDate.now());
        p.setMethod(method);
        p.setTransactionId(
                transactionId == null || transactionId.isBlank() ? null : transactionId.trim());
        p.setNextExpiry(member.getExpiryDate());
        payments.save(p);

        User u = member.getUser();
        notifications.sendToOwners("info",
                "Payment received: ₹" + amount + " from " + u.getName()
                        + " (" + u.getMemberCode() + ") via " + method, null);
    }
}