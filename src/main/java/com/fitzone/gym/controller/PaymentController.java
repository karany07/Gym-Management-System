package com.fitzone.gym.controller;

import com.fitzone.gym.model.Payment;
import com.fitzone.gym.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/owner/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentRepository payments;

    @GetMapping
    public String list(@RequestParam(defaultValue = "") String q, Model model) {
        String text = q.trim().toLowerCase();

        List<Payment> result = payments.findAllByOrderByPaymentDateDescIdDesc().stream()
                .filter(p -> text.isEmpty()
                        || p.getMember().getUser().getName().toLowerCase().contains(text)
                        || (p.getMember().getUser().getMemberCode() != null
                            && p.getMember().getUser().getMemberCode().toLowerCase().contains(text))
                        || (p.getTransactionId() != null
                            && p.getTransactionId().toLowerCase().contains(text)))
                .toList();

        model.addAttribute("payments", result);
        model.addAttribute("q", q);
        model.addAttribute("total", result.stream().mapToInt(Payment::getAmount).sum());
        return "payments";
    }
}