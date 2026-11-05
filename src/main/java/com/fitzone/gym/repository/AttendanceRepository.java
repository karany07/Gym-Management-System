package com.fitzone.gym.repository;

import com.fitzone.gym.model.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    List<Attendance> findByAttendanceDateOrderByCheckInTimeAsc(LocalDate date);
    List<Attendance> findByMemberIdOrderByAttendanceDateDesc(Long memberId);
    boolean existsByMemberIdAndAttendanceDate(Long memberId, LocalDate date);
    long countByAttendanceDate(LocalDate date);
}