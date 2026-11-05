package com.fitzone.gym.controller;

import com.fitzone.gym.model.Member;
import com.fitzone.gym.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Controller
@RequestMapping("/owner/verify")
@RequiredArgsConstructor
public class VerifyController {

    private final MemberRepository members;

    // Member ID ya mobile se check
    @GetMapping
    public String form(@RequestParam(required = false) String code, Model model) {
        model.addAttribute("today", LocalDate.now());
        if (code != null && !code.isBlank()) {
            String c = code.trim();
            Member m = members.findByUserMemberCode(c.toUpperCase())
                    .or(() -> members.findByUserMobile(c))
                    .orElse(null);
            if (m == null) model.addAttribute("error", "Is ID/mobile ka koi member nahi mila");
            else model.addAttribute("member", m);
        }
        return "verify";
    }

    // QR scan se aaya link
    @GetMapping("/{token}")
    public String scan(@PathVariable String token, Model model) {
        model.addAttribute("today", LocalDate.now());
        Member m = members.findByQrToken(token).orElse(null);
        if (m == null) model.addAttribute("error", "Ye QR code pehchana nahi gaya");
        else model.addAttribute("member", m);
        return "verify";
    }
}