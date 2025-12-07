package com.example.english_learning_platform.repository;

import com.example.english_learning_platform.entity.Plan;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Date;

public interface PlanRepository extends JpaRepository<Plan, Long> {
    // 根据用户ID和日期查询计划
    List<Plan> findByUserIdAndDate(Long userId, Date date);

    // 根据用户ID和日期范围查询计划
    List<Plan> findByUserIdAndDateBetween(Long userId, Date startDate, Date endDate);
}