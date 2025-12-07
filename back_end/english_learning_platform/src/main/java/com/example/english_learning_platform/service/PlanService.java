package com.example.english_learning_platform.service;

import com.example.english_learning_platform.entity.Plan;

import java.util.Date;
import java.util.List;

public interface PlanService {
    // 添加单条计划
    Plan addPlan(Plan plan);

    // 根据用户ID和日期查询计划
    List<Plan> getPlansByDate(Long userId, Date date);

    // 切换计划完成状态
    Plan toggleComplete(Long planId, Long userId);

    // 更新计划
    Plan updatePlan(Long planId, Long userId, Plan updatePlan);

    // 删除计划
    boolean deletePlan(Long planId, Long userId);

    // 批量保存某日计划（覆盖原有同日期计划）
    List<Plan> batchSavePlans(Long userId, Date date, List<Plan> plans);

    // 查询用户指定日期范围的计划（周视图/月视图用）
    List<Plan> getPlansByDateRange(Long userId, Date startDate, Date endDate);
}