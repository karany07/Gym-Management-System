package com.fitzone.gym.repository;

import com.fitzone.gym.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmailOrMobileOrMemberCode(String email, String mobile, String memberCode);
    boolean existsByMobile(String mobile);
    boolean existsByEmail(String email);
    boolean existsByRole(String role);
    List<User> findByRole(String role);
    boolean existsByEmailAndIdNot(String email, Long id);
    boolean existsByMobileAndIdNot(String mobile, Long id);
}