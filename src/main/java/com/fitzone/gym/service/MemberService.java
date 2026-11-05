package com.fitzone.gym.service;

import com.fitzone.gym.dto.MemberForm;
import com.fitzone.gym.model.Member;
import com.fitzone.gym.model.Plan;
import com.fitzone.gym.model.User;
import com.fitzone.gym.repository.MemberRepository;
import com.fitzone.gym.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MemberService {

    private static final Set<String> ALLOWED_EXT = Set.of("png", "jpg", "jpeg", "webp");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    private final UserRepository users;
    private final MemberRepository members;
    private final PasswordEncoder encoder;
    private final PaymentService payments;
    private final NotificationService notifications;

    @Value("${gym.upload-dir}")
    private String uploadDir;

    /** Server-side validation. Sab theek ho to null, warna error message. */
    public String validate(MemberForm f) {
        if (f.getFullName() == null || f.getFullName().isBlank())
            return "Naam zaroori hai";
        if (f.getMobile() == null || !f.getMobile().matches("[6-9]\\d{9}"))
            return "Sahi 10 digit mobile number daalein";
        if (f.getPassword() == null || f.getPassword().length() < 6)
            return "Password kam se kam 6 character ka ho";
        if (f.getPlanId() == null)
            return "Plan chuniye";
        if (f.getStartDate() == null)
            return "Start date chuniye";
        if (users.existsByMobile(f.getMobile()))
            return "Ye mobile number pehle se registered hai";

        String email = normalizeEmail(f.getEmail());
        if (email != null && users.existsByEmail(email))
            return "Ye email pehle se registered hai";

        String payError = PaymentService.validate(f.getPaymentMethod(), f.getTransactionId());
        if (payError != null)
            return payError;

        MultipartFile photo = f.getPhoto();
        if (photo != null && !photo.isEmpty()
                && !ALLOWED_EXT.contains(extension(photo.getOriginalFilename())))
            return "Photo sirf png, jpg, jpeg ya webp ho sakti hai";

        return null;
    }

    @Transactional
    public User register(MemberForm f, Plan plan) {
        User user = new User();
        user.setRole("MEMBER");
        user.setName(f.getFullName().trim());
        user.setMobile(f.getMobile());
        user.setEmail(normalizeEmail(f.getEmail()));
        user.setPasswordHash(encoder.encode(f.getPassword()));
        users.save(user);

        Member m = new Member();
        m.setUser(user);
        m.setPlan(plan);
        m.setDob(f.getDob());
        m.setGender(f.getGender());
        m.setStartDate(f.getStartDate());
        m.setJoinDate(f.getStartDate());
        m.setQrToken(UUID.randomUUID().toString());
        m.setExpiryDate(f.getStartDate().plusMonths(plan.getMonths()));   // Start + Duration = Expiry
        m.setAmountPaid(f.getAmount() == null ? 0 : f.getAmount());
        m.setPhoto(savePhoto(f.getPhoto()));
        members.save(m);

        user.setMemberCode(String.format("GYM%03d", m.getId()));          // GYM001, GYM002 ...

        notifications.sendToOwners("success",
                "New member registered: " + user.getName() + " (" + user.getMemberCode() + ")", null);

        if (m.getAmountPaid() > 0)
            payments.record(m, m.getAmountPaid(), f.getPaymentMethod(), f.getTransactionId());

        return user;
    }

    @Transactional
    public Member renew(Member m, Plan plan, LocalDate startDate, int amount,
                        String method, String transactionId) {
        m.setPlan(plan);
        m.setStartDate(startDate);
        m.setExpiryDate(startDate.plusMonths(plan.getMonths()));          // Start + Duration = Expiry
        m.setAmountPaid(m.getAmountPaid() + amount);                      // revenue me judta hai
        members.save(m);

        User u = m.getUser();
        String till = m.getExpiryDate().format(DATE_FMT);
        notifications.sendToOwners("success",
                "Membership renewed: " + u.getName() + " (" + u.getMemberCode() + ") till " + till, null);
        notifications.send(u, "success",
                "Your membership has been renewed till " + till + ". Thank you!", null);

        if (amount > 0)
            payments.record(m, amount, method, transactionId);

        return m;
    }

    private String normalizeEmail(String email) {
        return (email == null || email.isBlank()) ? null : email.trim().toLowerCase();
    }

    private String extension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }

    private String savePhoto(MultipartFile file) {
        if (file == null || file.isEmpty()) return null;
        String name = UUID.randomUUID() + "." + extension(file.getOriginalFilename());  // random naam
        try {
            Path dir = Path.of(uploadDir).toAbsolutePath();
            Files.createDirectories(dir);
            file.transferTo(dir.resolve(name));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return name;
    }
}