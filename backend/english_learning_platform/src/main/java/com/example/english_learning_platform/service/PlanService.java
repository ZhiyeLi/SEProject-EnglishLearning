package com.example.english_learning_platform.service;

import com.example.english_learning_platform.entity.Plan;
import com.example.english_learning_platform.repository.PlanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PlanService {
    
    private final PlanRepository planRepository;
    
    public PlanService(PlanRepository planRepository) {
        this.planRepository = planRepository;
    }
    
    public List<Plan> getAllPlans(Long userId) {
        return planRepository.findByUserIdOrderByDateDesc(userId);
    }
    
    public List<Plan> getPlansByDate(Long userId, LocalDate date) {
        return planRepository.findByUserIdAndDateOrderByStartTime(userId, date);
    }
    
    public List<Plan> getTodayPlans(Long userId) {
        return getPlansByDate(userId, LocalDate.now());
    }
    
    public Map<String, Object> getPlanStatistics(Long userId) {
        Long completedCount = planRepository.countByUserIdAndIfCompleted(userId, true);
        Long totalCount = (long) planRepository.findByUserIdOrderByDateDesc(userId).size();
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("completed", completedCount);
        stats.put("total", totalCount);
        stats.put("pending", totalCount - completedCount);
        
        return stats;
    }
    
    public LocalDate getFirstPlanDate(Long userId) {
        List<Plan> plans = planRepository.findByUserIdOrderByDateDesc(userId);
        if (plans.isEmpty()) {
            return null;
        }
        // 找到最早的日期（列表是倒序的，所以取最后一个）
        return plans.get(plans.size() - 1).getDate();
    }
    
    @Transactional
    public Plan createPlan(Plan plan) {
        return planRepository.save(plan);
    }
    
    @Transactional
    public Plan updatePlan(Long planId, Plan updates) {
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("计划不存在"));
        
        if (updates.getTitle() != null) plan.setTitle(updates.getTitle());
        if (updates.getDescription() != null) plan.setDescription(updates.getDescription());
        if (updates.getCategory() != null) plan.setCategory(updates.getCategory());
        if (updates.getPriority() != null) plan.setPriority(updates.getPriority());
        if (updates.getStartTime() != null) plan.setStartTime(updates.getStartTime());
        if (updates.getEndTime() != null) plan.setEndTime(updates.getEndTime());
        if (updates.getDate() != null) plan.setDate(updates.getDate());
        if (updates.getIfCompleted() != null) plan.setIfCompleted(updates.getIfCompleted());
        
        return planRepository.save(plan);
    }
    
    @Transactional
    public Plan toggleComplete(Long planId) {
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("计划不存在"));
        plan.setIfCompleted(!plan.getIfCompleted());
        return planRepository.save(plan);
    }
    
    @Transactional
    public void deletePlan(Long planId) {
        planRepository.deleteById(planId);
    }
    
    @Transactional
    public void batchDelete(List<Long> ids) {
        planRepository.deleteAllById(ids);
    }
}
