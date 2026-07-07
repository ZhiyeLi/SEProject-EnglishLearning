package com.example.english_learning_platform.service;

import com.example.english_learning_platform.entity.Plan;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface PlanService {

    List<Plan> getAllPlans(Long userId);

    List<Plan> getPlansByDate(Long userId, LocalDate date);

    List<Plan> getTodayPlans(Long userId);

    Map<String, Object> getPlanStatistics(Long userId);

    LocalDate getFirstPlanDate(Long userId);

    Plan createPlan(Plan plan);

    Plan updatePlan(Long planId, Plan updates);

    Plan toggleComplete(Long planId);

    void deletePlan(Long planId);

    void batchDelete(List<Long> ids);
}