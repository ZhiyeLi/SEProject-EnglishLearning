package com.example.english_learning_platform.controller;

import com.example.english_learning_platform.dto.UnreadCountDTO;
import com.example.english_learning_platform.dto.ApiResponse;
import com.example.english_learning_platform.entity.FriendRequest;
import com.example.english_learning_platform.entity.Message;
import com.example.english_learning_platform.entity.User;
import com.example.english_learning_platform.service.FriendService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import com.example.english_learning_platform.dto.FriendRankingDTO;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/friends")
public class FriendController {
    
    private final FriendService friendService;
    
    public FriendController(FriendService friendService) {
        this.friendService = friendService;
    }

    // 新增：好友周学习单词排行榜接口
    @GetMapping("/ranking/weekly")
    public ApiResponse<List<FriendRankingDTO>> getFriendWeeklyRanking(HttpServletRequest request) {
        try {
            // 1. 获取当前登录用户ID（复用项目现有逻辑：从request.getAttribute获取）
            Long currentUserId = (Long) request.getAttribute("userId");
            if (currentUserId == null) {
                return ApiResponse.error(401, "未登录或会话失效");
            }

            // 2. 调用Service层获取排行榜数据
            List<FriendRankingDTO> rankingList = friendService.getFriendWeeklyRanking(currentUserId);

            // 3. 按现有格式返回成功响应
            return ApiResponse.success(rankingList);
        } catch (Exception e) {
            // 统一异常处理，和现有接口保持一致
            return ApiResponse.error(e.getMessage());
        }
    }

    // 搜索添加好友用户接口
    @GetMapping("/search")
    public ApiResponse<List<User>> searchFriends(
            @RequestAttribute("userId") Long userId,
            @RequestParam String keyword) {

        try {
            List<User> users = friendService.searchNewFriends(keyword,userId);
            return ApiResponse.success(users);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PostMapping("/request")
    public ApiResponse<FriendRequest> sendFriendRequest(
            HttpServletRequest request,
            @RequestBody Map<String, Long> data) {
        try {
            Long senderId = (Long) request.getAttribute("userId");
            Long receiverId = data.get("receiverId");
            FriendRequest friendRequest = friendService.sendFriendRequest(senderId, receiverId);
            return ApiResponse.success(friendRequest);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/requests")
    public ApiResponse<List<Map<String, Object>>> getFriendRequests(HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            List<Map<String, Object>> requests = friendService.getFriendRequests(userId);
            return ApiResponse.success(requests);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PostMapping("/accept")
    public ApiResponse<String> acceptFriendRequest(
            HttpServletRequest request,
            @RequestBody Map<String, Long> data) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            Long requestId = data.get("requestId");
            friendService.acceptFriendRequest(requestId, userId);
            return ApiResponse.success("已接受好友请求");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PostMapping("/reject")
    public ApiResponse<String> rejectFriendRequest(
            HttpServletRequest request,
            @RequestBody Map<String, Long> data) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            Long requestId = data.get("requestId");
            friendService.rejectFriendRequest(requestId, userId);
            return ApiResponse.success("已拒绝好友请求");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/list")
    public ApiResponse<List<Map<String, Object>>> getFriendList(HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            List<Map<String, Object>> friends = friendService.getFriendList(userId);
            return ApiResponse.success(friends);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PostMapping("/message")
    public ApiResponse<Message> sendMessage(
            HttpServletRequest request,
            @RequestBody Map<String, Object> data) {
        try {
            Long senderId = (Long) request.getAttribute("userId");
            Long receiverId = Long.parseLong(data.get("receiverId").toString());
            String content = (String) data.get("content");
            Message message = friendService.sendMessage(senderId, receiverId, content);
            return ApiResponse.success(message);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/messages")
    public ApiResponse<List<Message>> getMessageList(
            HttpServletRequest request,
            @RequestParam Long friendId) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            List<Message> messages = friendService.getMessageList(userId, friendId);
            return ApiResponse.success(messages);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/total-unread-count")
    public ApiResponse<Long> getUnreadCount(HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            Long count = friendService.getUnreadCount(userId);
            return ApiResponse.success(count);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    // 获取按好友分组的未读计数（前端核心使用）
    @GetMapping("/unread-count")
    public ApiResponse<List<UnreadCountDTO>> getUnreadCountGroupByFriend(HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            List<UnreadCountDTO> unreadCountList = friendService.getUnreadCountGroupByFriend(userId);
            return ApiResponse.success(unreadCountList); // 返回数组格式：[{friendId:123, count:5}, ...]
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    // 标记消息为已读的接口（前端选中好友后调用）
    @PostMapping("/mark-as-read")
    public ApiResponse<String> markMessagesAsRead(
            HttpServletRequest request,
            @RequestBody Map<String, Long> data
    ) {
        try {
            Long userId = (Long) request.getAttribute("userId"); // 当前用户（接收者）
            Long friendId = data.get("friendId"); // 好友ID（发送者）
            friendService.markMessagesAsRead(userId, friendId);
            return ApiResponse.success("消息已标记为已读");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
