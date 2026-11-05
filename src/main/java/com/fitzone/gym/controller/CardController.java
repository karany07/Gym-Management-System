package com.fitzone.gym.controller;

import com.fitzone.gym.model.Member;
import com.fitzone.gym.repository.MemberRepository;
import com.fitzone.gym.service.CardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class CardController {

    private final MemberRepository members;
    private final CardService cards;

    // Member sirf apna card dekhta hai (ID login se aati hai, URL se nahi)
    @GetMapping("/member/card")
    public String myCard(Authentication auth, Model model) {
        Member m = members.findByUserId(Long.parseLong(auth.getName())).orElseThrow();
        return show(m, model, false);
    }

    // Owner kisi bhi member ka card khol sakta hai
    @GetMapping("/owner/members/{id}/card")
    public String ownerCard(@PathVariable Long id, Model model, RedirectAttributes ra) {
        Member m = members.findById(id).orElse(null);
        if (m == null) {
            ra.addFlashAttribute("error", "Member nahi mila");
            return "redirect:/owner/members";
        }
        return show(m, model, true);
    }

    private String show(Member m, Model model, boolean owner) {
        model.addAttribute("member", m);
        model.addAttribute("qrSvg", cards.qrSvgFor(m));
        model.addAttribute("joinDate", m.getJoinDate() != null ? m.getJoinDate() : m.getStartDate());
        model.addAttribute("backUrl", owner ? "/owner/members" : "/member/dashboard");
        return "member_card";
    }
}