package com.fitzone.gym.controller;

import com.fitzone.gym.model.Plan;
import com.fitzone.gym.repository.MemberRepository;
import com.fitzone.gym.repository.PlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/owner/plans")
@RequiredArgsConstructor
public class PlanController {

    private final PlanRepository plans;
    private final MemberRepository members;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("plans", plans.findAllByOrderByMonthsAsc());
        model.addAttribute("plan", new Plan());          // khali add form
        return "plans";
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model, RedirectAttributes ra) {
        Plan plan = plans.findById(id).orElse(null);
        if (plan == null) {
            ra.addFlashAttribute("error", "Plan nahi mila");
            return "redirect:/owner/plans";
        }
        model.addAttribute("plans", plans.findAllByOrderByMonthsAsc());
        model.addAttribute("plan", plan);                // bhara hua edit form
        return "plans";
    }

    @PostMapping("/save")
    public String save(@RequestParam(required = false) Long id,
                       @RequestParam(required = false) String name,
                       @RequestParam(required = false) Integer months,
                       @RequestParam(required = false) Integer price,
                       RedirectAttributes ra) {

        String n = name == null ? "" : name.trim();
        String error = null;
        if (n.isEmpty())
            error = "Plan ka naam zaroori hai";
        else if (months == null || months < 1 || months > 60)
            error = "Duration 1 se 60 mahine ke beech ho";
        else if (price == null || price < 0)
            error = "Sahi price daalein";

        if (error != null) {
            ra.addFlashAttribute("error", error);
            return "redirect:/owner/plans" + (id != null ? "/" + id + "/edit" : "");
        }

        Plan plan = (id == null) ? new Plan() : plans.findById(id).orElse(new Plan());
        plan.setName(n);
        plan.setMonths(months);
        plan.setPrice(price);
        plans.save(plan);

        ra.addFlashAttribute("success", id == null ? "Plan add ho gaya" : "Plan update ho gaya");
        return "redirect:/owner/plans";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        if (members.existsByPlanId(id)) {
            ra.addFlashAttribute("error", "Is plan par members hain, isliye delete nahi ho sakta");
        } else {
            plans.deleteById(id);
            ra.addFlashAttribute("success", "Plan delete ho gaya");
        }
        return "redirect:/owner/plans";
    }
}