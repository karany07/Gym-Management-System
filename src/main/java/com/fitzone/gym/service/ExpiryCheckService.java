package com.fitzone.gym.service;

import com.fitzone.gym.model.Member;
import com.fitzone.gym.model.User;
import com.fitzone.gym.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExpiryCheckService {

    private final MemberRepository members;
    private final NotificationService notifications;

    /** Roz subah 6 baje */
    @Scheduled(cron = "0 0 6 * * *", zone = "Asia/Kolkata")
    public void dailyRun() {
        log.info(">>> Daily expiry check: {} nayi notifications", runCheck());
    }

    /** Server start hote hi bhi, taaki band rehne par koi din na chhute */
    @EventListener(ApplicationReadyEvent.class)
    public void startupRun() {
        log.info(">>> Startup expiry check: {} nayi notifications", runCheck());
    }

    /** Sabhi members check karo. Kitni nayi notifications bani wo wapas deta hai. */
    public int runCheck() {
        int created = 0;
        for (Member m : members.findAll()) {
            created += check(m);
        }
        return created;
    }

    private String stageFor(long d) {
        if (d <= -2) return "ARCHIVED";
        if (d <= 0)  return "EXPIRED";
        if (d == 1)  return "D1";
        if (d <= 3)  return "D3";
        if (d <= 7)  return "D7";
        return null;                       // abhi kuch nahi karna
    }

    private int check(Member m) {
        long d = m.getDaysLeft();
        String stage = stageFor(d);
        if (stage == null) return 0;

        User u = m.getUser();
        String who = u.getName() + " (" + u.getMemberCode() + ")";
        // Nayi expiry date = nayi key, isliye renew ke baad alerts dobara shuru ho jate hain
        String key = stage + ":" + m.getId() + ":" + m.getExpiryDate();
        String days = d + (d == 1 ? " day" : " days");
        int n = 0;

        switch (stage) {
            case "D7" -> {
                n += notifications.send(u, "warning",
                        "Your gym membership will expire in " + days + ". Please renew your membership.", key);
                n += notifications.sendToOwners("warning",
                        who + "'s membership expires in " + days + ".", key);
            }
            case "D3" -> {
                n += notifications.send(u, "danger",
                        "Red alert: your gym membership will expire in " + days + ". Please renew your membership.", key);
                n += notifications.sendToOwners("danger",
                        "Red alert: " + who + "'s membership expires in " + days + ".", key);
            }
            case "D1" -> {
                n += notifications.send(u, "danger",
                        "Urgent: your gym membership expires tomorrow. Please renew today.", key);
                n += notifications.sendToOwners("danger",
                        "Urgent: " + who + "'s membership expires tomorrow.", key);
            }
            case "EXPIRED" -> {
                n += notifications.send(u, "danger",
                        "Your gym membership has expired. Please renew to continue.", key);
                n += notifications.sendToOwners("danger",
                        who + "'s membership has expired.", key);
            }
            case "ARCHIVED" -> n += notifications.sendToOwners("info",
                        who + " moved to Archived Members (2+ days after expiry).", key);
            default -> { }
        }
        return n;
    }
}
