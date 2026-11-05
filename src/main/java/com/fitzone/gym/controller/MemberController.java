package com.fitzone.gym.controller;

import com.fitzone.gym.model.Member;
import com.fitzone.gym.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberRepository members;

    @GetMapping("/dashboard")
    public String dashboard(Authentication auth, Model model) {
        // ID login se aati hai, URL se nahi, isliye member dusre ka data nahi dekh sakta
        Member m = members.findByUserId(Long.parseLong(auth.getName())).orElseThrow();
        model.addAttribute("member", m);
        return "member_dashboard";
    }
}