package com.example.english_learning_platform.controller;

import com.example.english_learning_platform.entity.Plan;
import com.example.english_learning_platform.service.PlanService;
import com.example.english_learning_platform.util.JwtUtil;
import com.example.english_learning_platform.util.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/plans")
@RequiredArgsConstructor
public class PlanController {

    private final PlanService planService;
    private final JwtUtil jwtUtil;

    // 添加单条计划
    @PostMapping
    public Result addPlan(@RequestBody Plan plan, @RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.getUserIdFromToken(token.replace("Bearer ", ""));
        plan.setUserId(userId);
        return Result.success(planService.addPlan(plan));
    }

    // 获取指定日期的计划
    @GetMapping("/date/{date}")
    public Result getPlansByDate(
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") Date date,
            @RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.getUserIdFromToken(token.replace("Bearer ", ""));
        List<Plan> plans = planService.getPlansByDate(userId, date);
        return Result.success(plans);
    }

    // 切换计划完成状态
    @PutMapping("/{id}/complete")
    public Result toggleComplete(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.getUserIdFromToken(token.replace("Bearer ", ""));
        return Result.success(planService.toggleComplete(id, userId));
    }

    // 更新计划
    @PutMapping("/{id}")
    public Result updatePlan(
            @PathVariable Long id,
            @RequestBody Plan plan,
            @RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.getUserIdFromToken(token.replace("Bearer ", ""));
        return Result.success(planService.updatePlan(id, userId, plan));
    }

    // 删除计划
    @DeleteMapping("/{id}")
    public Result deletePlan(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.getUserIdFromToken(token.replace("Bearer ", ""));
        boolean success = planService.deletePlan(id, userId);
        return success ? Result.success(null) : Result.error("删除失败");
    }

    // 批量保存某日计划
    @PostMapping("/batch/{date}")
    public Result batchSavePlans(
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") Date date,
            @RequestBody List<Plan> plans,
            @RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.getUserIdFromToken(token.replace("Bearer ", ""));
        List<Plan> savedPlans = planService.batchSavePlans(userId, date, plans);
        return Result.success(savedPlans);
    }

    // 查询日期范围的计划（周/月视图）
    @GetMapping("/range")
    public Result getPlansByRange(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
            @RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.getUserIdFromToken(token.replace("Bearer ", ""));
        List<Plan> plans = planService.getPlansByDateRange(userId, startDate, endDate);
        return Result.success(plans);
    }
}