package com.fitzone.gym.controller;

import com.fitzone.gym.model.Attendance;
import com.fitzone.gym.model.Member;
import com.fitzone.gym.repository.AttendanceRepository;
import com.fitzone.gym.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceRepository attendance;
    private final MemberRepository members;

    // ---------------- OWNER ----------------

    @GetMapping("/owner/attendance")
    public String ownerView(@RequestParam(required = false)
                            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                            Model model) {
        LocalDate today = LocalDate.now();
        LocalDate day = (date == null || date.isAfter(today)) ? today : date;

        List<Attendance> present = attendance.findByAttendanceDateOrderByCheckInTimeAsc(day);
        Set<Long> presentIds = present.stream()
                .map(a -> a.getMember().getId())
                .collect(Collectors.toSet());

        // Jinki membership us date par chal rahi thi aur abhi present mark nahi hue
        List<Member> pending = members.findAllByOrderByIdDesc().stream()
                .filter(m -> day.isBefore(m.getExpiryDate()))
                .filter(m -> !presentIds.contains(m.getId()))
                .toList();

        model.addAttribute("day", day);
        model.addAttribute("present", present);
        model.addAttribute("pending", pending);
        return "attendance";
    }

    @PostMapping("/owner/attendance/mark")
    public String mark(@RequestParam Long memberId,
                       @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                       RedirectAttributes ra) {
        String back = "redirect:/owner/attendance?date=" + date;
        LocalDate today = LocalDate.now();

        Member m = members.findById(memberId).orElse(null);
        if (m == null) {
            ra.addFlashAttribute("error", "Member nahi mila");
            return back;
        }
        if (date.isAfter(today)) {
            ra.addFlashAttribute("error", "Aage ki date ki attendance nahi lag sakti");
            return back;
        }
        if (!date.isBefore(m.getExpiryDate())) {
            ra.addFlashAttribute("error",
                    m.getUser().getName() + " ki membership expire ho chuki hai. Pehle renew karein.");
            return back;
        }
        if (attendance.existsByMemberIdAndAttendanceDate(m.getId(), date)) {
            ra.addFlashAttribute("error", m.getUser().getName() + " pehle se present hai");
            return back;
        }

        Attendance a = new Attendance();
        a.setMember(m);
        a.setAttendanceDate(date);
        a.setCheckInTime(date.equals(today) ? LocalTime.now().withSecond(0).withNano(0) : null);
        attendance.save(a);

        ra.addFlashAttribute("success", m.getUser().getName() + " present mark ho gaya");
        return back;
    }

    // ---------------- MEMBER ----------------

    @GetMapping("/member/attendance")
    public String memberView(Authentication auth, Model model) {
        // ID login se aati hai, URL se nahi, isliye member sirf apni attendance dekhega
        Member m = members.findByUserId(Long.parseLong(auth.getName())).orElseThrow();
        List<Attendance> list = attendance.findByMemberIdOrderByAttendanceDateDesc(m.getId());

        LocalDate monthStart = LocalDate.now().withDayOfMonth(1);
        long thisMonth = list.stream()
                .filter(a -> !a.getAttendanceDate().isBefore(monthStart))
                .count();

        model.addAttribute("member", m);
        model.addAttribute("list", list);
        model.addAttribute("thisMonth", thisMonth);
        return "member_attendance";
    }
}