package com.example.english_learning_platform.controller;

import com.example.english_learning_platform.dto.ApiResponse;
import com.example.english_learning_platform.entity.FriendRequest;
import com.example.english_learning_platform.entity.Message;
import com.example.english_learning_platform.entity.User;
import com.example.english_learning_platform.service.FriendService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/friends")
public class FriendController {
    
    private final FriendService friendService;
    
    public FriendController(FriendService friendService) {
        this.friendService = friendService;
    }

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
    public ApiResponse<List<FriendRequest>> getFriendRequests(HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            List<FriendRequest> requests = friendService.getFriendRequests(userId);
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
    
    @GetMapping("/unread-count")
    public ApiResponse<Long> getUnreadCount(HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            Long count = friendService.getUnreadCount(userId);
            return ApiResponse.success(count);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
