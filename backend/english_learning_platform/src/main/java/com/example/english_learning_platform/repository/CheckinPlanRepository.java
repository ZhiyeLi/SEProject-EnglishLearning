package com.example.english_learning_platform.repository;

import com.example.english_learning_platform.entity.CheckinPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CheckinPlanRepository extends JpaRepository<CheckinPlan, Long> {
    Optional<CheckinPlan> findByUserIdAndStatus(Long userId, String status);
    List<CheckinPlan> findByUserId(Long userId);
}
