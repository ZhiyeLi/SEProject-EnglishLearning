package com.example.english_learning_platform.service.impl;

import com.example.english_learning_platform.entity.Plan;
import com.example.english_learning_platform.repository.PlanRepository;
import com.example.english_learning_platform.service.PlanService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PlanServiceImpl implements PlanService {

    private final PlanRepository planRepository;

    /**
     * 添加单条计划
     */
    @Override
    public Plan addPlan(Plan plan) {
        // 基础参数校验
        if (plan.getTitle() == null || plan.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("计划标题不能为空");
        }
        if (plan.getCategory() == null || plan.getCategory().trim().isEmpty()) {
            plan.setCategory("默认"); // 兜底默认分类
        }
        if (plan.getCompleted() == null) {
            plan.setCompleted(false); // 默认未完成
        }
        return planRepository.save(plan);
    }

    /**
     * 根据用户ID和日期查询计划
     */
    @Override
    public List<Plan> getPlansByDate(Long userId, Date date) {
        // 校验参数
        if (userId == null || date == null) {
            throw new IllegalArgumentException("用户ID和日期不能为空");
        }
        return planRepository.findByUserIdAndDate(userId, date);
    }

    /**
     * 切换计划完成状态
     */
    @Override
    public Plan toggleComplete(Long planId, Long userId) {
        // 1. 查询计划并校验归属权
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new EntityNotFoundException("计划不存在"));
        if (!plan.getUserId().equals(userId)) {
            throw new SecurityException("无权限操作该计划");
        }

        // 2. 切换完成状态
        plan.setCompleted(!plan.getCompleted());
        return planRepository.save(plan);
    }

    /**
     * 更新计划
     */
    @Override
    public Plan updatePlan(Long planId, Long userId, Plan updatePlan) {
        // 1. 查询计划并校验归属权
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new EntityNotFoundException("计划不存在"));
        if (!plan.getUserId().equals(userId)) {
            throw new SecurityException("无权限操作该计划");
        }

        // 2. 覆盖可更新字段（避免修改用户ID等核心字段）
        if (updatePlan.getTitle() != null && !updatePlan.getTitle().trim().isEmpty()) {
            plan.setTitle(updatePlan.getTitle());
        }
        if (updatePlan.getCategory() != null) {
            plan.setCategory(updatePlan.getCategory());
        }
        if (updatePlan.getDate() != null) {
            plan.setDate(updatePlan.getDate());
        }
        if (updatePlan.getNote() != null) {
            plan.setNote(updatePlan.getNote());
        }
        if (updatePlan.getPriority() != null) {
            plan.setPriority(updatePlan.getPriority());
        }
        // 完成状态单独维护，不通过通用更新接口修改

        return planRepository.save(plan);
    }

    /**
     * 删除计划
     */
    @Override
    public boolean deletePlan(Long planId, Long userId) {
        // 1. 查询计划并校验归属权
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new EntityNotFoundException("计划不存在"));
        if (!plan.getUserId().equals(userId)) {
            throw new SecurityException("无权限操作该计划");
        }

        // 2. 删除计划
        planRepository.delete(plan);
        return true;
    }

    /**
     * 批量保存某日计划（先删后加，覆盖原有数据）
     */
    @Override
    @Transactional // 事务保证：删除和新增要么都成功，要么都失败
    public List<Plan> batchSavePlans(Long userId, Date date, List<Plan> plans) {
        // 1. 校验参数
        if (userId == null || date == null || plans == null) {
            throw new IllegalArgumentException("参数不能为空");
        }

        // 2. 删除该用户该日期的所有旧计划
        List<Plan> oldPlans = planRepository.findByUserIdAndDate(userId, date);
        if (!oldPlans.isEmpty()) {
            planRepository.deleteAll(oldPlans);
        }

        // 3. 批量保存新计划（补充用户ID和日期）
        plans.forEach(plan -> {
            plan.setUserId(userId);
            plan.setDate(date);
            if (plan.getCompleted() == null) {
                plan.setCompleted(false);
            }
            if (plan.getCategory() == null || plan.getCategory().trim().isEmpty()) {
                plan.setCategory("默认");
            }
        });

        return planRepository.saveAll(plans);
    }

    /**
     * 查询用户指定日期范围的计划（周/月视图）
     */
    @Override
    public List<Plan> getPlansByDateRange(Long userId, Date startDate, Date endDate) {
        // 校验参数
        if (userId == null || startDate == null || endDate == null) {
            throw new IllegalArgumentException("用户ID和日期范围不能为空");
        }
        if (startDate.after(endDate)) {
            throw new IllegalArgumentException("开始日期不能晚于结束日期");
        }

        return planRepository.findByUserIdAndDateBetween(userId, startDate, endDate);
    }
}