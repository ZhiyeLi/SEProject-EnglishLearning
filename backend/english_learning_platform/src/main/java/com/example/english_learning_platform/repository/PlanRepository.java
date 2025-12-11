package com.example.english_learning_platform.repository;

import com.example.english_learning_platform.entity.Plan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface PlanRepository extends JpaRepository<Plan, Long> {
    List<Plan> findByUserIdOrderByDateDesc(Long userId);
    List<Plan> findByUserIdAndDateOrderByStartTime(Long userId, LocalDate date);
    List<Plan> findByUserIdAndDateBetweenOrderByDate(Long userId, LocalDate startDate, LocalDate endDate);
    Long countByUserIdAndIfCompleted(Long userId, Boolean ifCompleted);
}
