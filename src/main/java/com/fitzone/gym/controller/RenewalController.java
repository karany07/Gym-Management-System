package com.fitzone.gym.controller;

import com.fitzone.gym.model.Member;
import com.fitzone.gym.model.Plan;
import com.fitzone.gym.repository.MemberRepository;
import com.fitzone.gym.repository.PlanRepository;
import com.fitzone.gym.service.MemberService;
import com.fitzone.gym.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Controller
@RequestMapping("/owner/members")
@RequiredArgsConstructor
public class RenewalController {

    private final MemberRepository members;
    private final PlanRepository plans;
    private final MemberService memberService;

    @GetMapping("/{id}/renew")
    public String form(@PathVariable Long id, Model model, RedirectAttributes ra) {
        Member m = members.findById(id).orElse(null);
        if (m == null) {
            ra.addFlashAttribute("error", "Member nahi mila");
            return "redirect:/owner/members";
        }

        // Chal rahi membership ho to purani expiry se aage badhao, expire ho chuki ho to aaj se
        LocalDate suggested = m.getExpiryDate().isAfter(LocalDate.now())
                ? m.getExpiryDate() : LocalDate.now();

        model.addAttribute("member", m);
        model.addAttribute("plans", plans.findAllByOrderByMonthsAsc());
        model.addAttribute("suggestedStart", suggested);
        return "renew_member";
    }

    @PostMapping("/{id}/renew")
    public String renew(@PathVariable Long id,
                        @RequestParam(required = false) Long planId,
                        @RequestParam(required = false)
                        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                        @RequestParam(required = false) Integer amount,
                        @RequestParam(required = false) String paymentMethod,
                        @RequestParam(required = false) String transactionId,
                        RedirectAttributes ra) {

        Member m = members.findById(id).orElse(null);
        if (m == null) {
            ra.addFlashAttribute("error", "Member nahi mila");
            return "redirect:/owner/members";
        }

        Plan plan = (planId == null) ? null : plans.findById(planId).orElse(null);
        String error = null;
        if (plan == null)
            error = "Plan chuniye";
        else if (startDate == null)
            error = "Start date chuniye";
        else if (amount == null || amount < 0)
            error = "Sahi amount daalein";
        else
            error = PaymentService.validate(paymentMethod, transactionId);

        if (error != null) {
            ra.addFlashAttribute("error", error);
            return "redirect:/owner/members/" + id + "/renew";
        }

        memberService.renew(m, plan, startDate, amount, paymentMethod, transactionId);

        ra.addFlashAttribute("success", "Membership renew ho gayi. Nayi expiry: "
                + m.getExpiryDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
                + " (" + m.getStatus() + ")");
        return "redirect:/owner/members";
    }
}