package com.fitzone.gym.repository;

import com.fitzone.gym.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    List<Member> findAllByOrderByIdDesc();
    Optional<Member> findByUserId(Long userId);
    boolean existsByPlanId(Long planId);
    Optional<Member> findByQrToken(String qrToken);
    Optional<Member> findByUserMemberCode(String memberCode);
    Optional<Member> findByUserMobile(String mobile);
}