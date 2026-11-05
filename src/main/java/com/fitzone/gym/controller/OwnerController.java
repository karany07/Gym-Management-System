package com.fitzone.gym.controller;

import com.fitzone.gym.dto.MemberForm;
import com.fitzone.gym.model.Member;
import com.fitzone.gym.model.Plan;
import com.fitzone.gym.model.User;
import com.fitzone.gym.repository.AttendanceRepository;
import com.fitzone.gym.repository.MemberRepository;
import com.fitzone.gym.repository.PlanRepository;
import com.fitzone.gym.repository.UserRepository;
import com.fitzone.gym.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Controller
@RequestMapping("/owner")
@RequiredArgsConstructor
public class OwnerController {

    private final MemberRepository members;
    private final PlanRepository plans;
    private final UserRepository users;
    private final MemberService memberService;
    private final AttendanceRepository attendance;

    @GetMapping("/dashboard")
    public String dashboard(Authentication auth, Model model) {
        User owner = users.findById(Long.parseLong(auth.getName())).orElseThrow();
        List<Member> all = members.findAll();
        LocalDate monthStart = LocalDate.now().withDayOfMonth(1);

        model.addAttribute("owner", owner);
        model.addAttribute("total", all.size());
        model.addAttribute("todayCount", attendance.countByAttendanceDate(LocalDate.now()));
        model.addAttribute("activeCount", count(all, "active"));
        model.addAttribute("expiringCount", count(all, "expiring"));
        model.addAttribute("expiredCount", count(all, "expired"));
        model.addAttribute("archivedCount", count(all, "archived"));
        model.addAttribute("newThisMonth", all.stream()
                .filter(m -> m.getJoinDate() != null && !m.getJoinDate().isBefore(monthStart))
                .count());
        model.addAttribute("revenue", all.stream().mapToInt(Member::getAmountPaid).sum());
        model.addAttribute("expiringList", all.stream()
                .filter(m -> m.getStatus().equals("expiring"))
                .sorted(Comparator.comparing(Member::getExpiryDate))
                .toList());
        model.addAttribute("recentMembers", members.findAllByOrderByIdDesc().stream()
                .limit(5)
                .toList());
        return "owner_dashboard";
    }

    @GetMapping("/members")
    public String list(@RequestParam(defaultValue = "") String q,
                       @RequestParam(defaultValue = "all") String status,
                       Model model) {
        String text = q.trim().toLowerCase();

        List<Member> result = members.findAllByOrderByIdDesc().stream()
                .filter(m -> text.isEmpty()
                        || m.getUser().getName().toLowerCase().contains(text)
                        || (m.getUser().getMobile() != null && m.getUser().getMobile().contains(text))
                        || (m.getUser().getMemberCode() != null
                            && m.getUser().getMemberCode().toLowerCase().contains(text)))
                .filter(m -> status.equals("all") || m.getStatus().equals(status))
                .toList();

        model.addAttribute("members", result);
        model.addAttribute("q", q);
        model.addAttribute("status", status);
        return "members";
    }

    @GetMapping("/members/add")
    public String addForm(Model model) {
        model.addAttribute("plans", plans.findAllByOrderByMonthsAsc());
        return "add_member";
    }

    @PostMapping("/members/add")
    public String add(@ModelAttribute MemberForm form, RedirectAttributes ra) {
        String error = memberService.validate(form);
        Plan plan = form.getPlanId() == null ? null : plans.findById(form.getPlanId()).orElse(null);
        if (error == null && plan == null) error = "Plan chuniye";

        if (error != null) {
            ra.addFlashAttribute("error", error);
            return "redirect:/owner/members/add";
        }

        User saved = memberService.register(form, plan);
        ra.addFlashAttribute("success", "Member add ho gaya. Member ID: " + saved.getMemberCode());
        return "redirect:/owner/members";
    }

    private long count(List<Member> all, String status) {
        return all.stream().filter(m -> m.getStatus().equals(status)).count();
    }
}