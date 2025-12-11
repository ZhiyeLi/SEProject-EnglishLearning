package com.example.english_learning_platform.controller;

import com.example.english_learning_platform.dto.ApiResponse;
import com.example.english_learning_platform.entity.Plan;
import com.example.english_learning_platform.service.PlanService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/plans")
public class PlanController {
    
    private final PlanService planService;
    
    public PlanController(PlanService planService) {
        this.planService = planService;
    }
    
    @GetMapping
    public ApiResponse<List<Plan>> getAllPlans(HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            List<Plan> plans = planService.getAllPlans(userId);
            return ApiResponse.success(plans);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/today")
    public ApiResponse<List<Plan>> getTodayPlans(HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            List<Plan> plans = planService.getTodayPlans(userId);
            return ApiResponse.success(plans);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/{date}")
    public ApiResponse<List<Plan>> getPlansByDate(HttpServletRequest request, @PathVariable String date) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            LocalDate localDate = LocalDate.parse(date);
            List<Plan> plans = planService.getPlansByDate(userId, localDate);
            return ApiResponse.success(plans);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/statistics")
    public ApiResponse<Map<String, Object>> getStatistics(HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            Map<String, Object> stats = planService.getPlanStatistics(userId);
            return ApiResponse.success(stats);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PostMapping
    public ApiResponse<Plan> createPlan(HttpServletRequest request, @RequestBody Plan plan) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            plan.setUserId(userId);
            Plan created = planService.createPlan(plan);
            return ApiResponse.success(created);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PutMapping("/{id}")
    public ApiResponse<Plan> updatePlan(@PathVariable Long id, @RequestBody Plan plan) {
        try {
            Plan updated = planService.updatePlan(id, plan);
            return ApiResponse.success(updated);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PutMapping("/{id}/complete")
    public ApiResponse<Plan> toggleComplete(@PathVariable Long id) {
        try {
            Plan plan = planService.toggleComplete(id);
            return ApiResponse.success(plan);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    public ApiResponse<String> deletePlan(@PathVariable Long id) {
        try {
            planService.deletePlan(id);
            return ApiResponse.success("删除成功");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @DeleteMapping("/batch")
    public ApiResponse<String> batchDelete(@RequestBody Map<String, List<Long>> data) {
        try {
            List<Long> ids = data.get("ids");
            planService.batchDelete(ids);
            return ApiResponse.success("批量删除成功");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
