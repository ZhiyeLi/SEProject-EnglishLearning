package com.example.english_learning_platform.service;

import com.example.english_learning_platform.dto.UnreadCountDTO;
import com.example.english_learning_platform.entity.*;
import com.example.english_learning_platform.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class FriendService {
    
    private final UserRepository userRepository;
    private final FriendRepository friendRepository;
    private final FriendRequestRepository friendRequestRepository;
    private final MessageRepository messageRepository;
    
    public FriendService(UserRepository userRepository,
                        FriendRepository friendRepository,
                        FriendRequestRepository friendRequestRepository,
                        MessageRepository messageRepository) {
        this.userRepository = userRepository;
        this.friendRepository = friendRepository;
        this.friendRequestRepository = friendRequestRepository;
        this.messageRepository = messageRepository;
    }

//    public List<User> searchFriends(String keyword, Long currentUserId) {
//        // 1. 空关键词处理：返回空列表
//        if (keyword == null || keyword.trim().isEmpty()) {
//            return Collections.emptyList();
//        }
//
//        // 2. 处理关键词：去除空格，尝试转换为Long类型的目标用户ID（targetUserId）
//        String trimmedKeyword = keyword.trim();
//        Long targetUserId = null;
//        try {
//            targetUserId = Long.valueOf(trimmedKeyword);
//        } catch (NumberFormatException e) {
//            // 关键词不是数字，targetUserId保持null
//        }
//
//        // 3. 获取当前用户的所有好友ID列表（从Friend表中查询）
//        List<Friend> userFriends = friendRepository.findByUserId(currentUserId);
//        List<Long> friendIds = userFriends.stream()
//                .map(Friend::getFriendId)
//                .collect(Collectors.toList());
//
//        // 4. 若没有好友，直接返回空列表
//        if (friendIds.isEmpty()) {
//            return Collections.emptyList();
//        }
//
//        // 5. 数据库层面查询：好友ID在列表中 + 匹配用户ID/用户名关键词
//        List<User> matchedFriends = userRepository.findByFriendIdsAndKeyword(
//                friendIds,
//                targetUserId,
//                trimmedKeyword
//        );
//
//        // 6. 最终过滤：排除自己（保险逻辑）+ 限制返回数量（前20条）
//        return matchedFriends.stream()
//                .filter(user -> !user.getUserId().equals(currentUserId))
//                .limit(20)
//                .collect(Collectors.toList());
//    }

    public List<User> searchNewFriends(String keyword, Long currentUserId) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return Collections.emptyList();
        }

        // 先查出所有符合关键词的用户 (包含自己和好友)
        List<User> allMatches = userRepository.findByUserNameContainingOrUserEmailContaining(keyword, keyword);

        // 查出当前用户的所有好友 ID
        Set<Long> myFriendIds = friendRepository.findByUserId(currentUserId)
                .stream()
                .map(friend -> friend.getFriendId()) // 获取好友的ID字段
                .collect(Collectors.toSet());

        // 在内存中过滤
        return allMatches.stream()
                // 排除自己 (比较 userId)
                .filter(user -> !user.getUserId().equals(currentUserId))
                // 排除已存在的好友
                .filter(user -> !myFriendIds.contains(user.getUserId()))
                // 限制返回数量 (例如只返回前 20 个)
                .limit(20)
                .collect(Collectors.toList());
    }

    @Transactional
    public FriendRequest sendFriendRequest(Long senderId, Long receiverId) {
        // 检查是否已经是好友
        if (friendRepository.existsByUserIdAndFriendId(senderId, receiverId) ||
            friendRepository.existsByUserIdAndFriendId(receiverId, senderId)) {
            throw new RuntimeException("已经是好友关系");
        }
        
        // 检查是否已发送过请求
        if (friendRequestRepository.existsBySenderIdAndReceiverIdAndStatus(senderId, receiverId, "pending")) {
            throw new RuntimeException("已发送过好友请求");
        }
        
        FriendRequest request = new FriendRequest();
        request.setSenderId(senderId);
        request.setReceiverId(receiverId);
        request.setStatus("pending");
        
        return friendRequestRepository.save(request);
    }
    
    public List<FriendRequest> getFriendRequests(Long userId) {
        return friendRequestRepository.findByReceiverIdAndStatus(userId, "pending");
    }
    
    @Transactional
    public void acceptFriendRequest(Long requestId, Long userId) {
        FriendRequest request = friendRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("好友请求不存在"));
        
        if (!request.getReceiverId().equals(userId)) {
            throw new RuntimeException("无权限操作");
        }

        // 建立正向关系：申请人 -> 我
        Friend forwardShip = new Friend();
        forwardShip.setUserId(request.getSenderId());
        forwardShip.setFriendId(request.getReceiverId());
        friendRepository.save(forwardShip);

        // 建立反向关系：我 -> 申请人 (新增部分)
        Friend backwardShip = new Friend();
        backwardShip.setUserId(request.getReceiverId());
        backwardShip.setFriendId(request.getSenderId());
        friendRepository.save(backwardShip);
        
        // 更新请求状态
        request.setStatus("accepted");
        friendRequestRepository.save(request);
    }
    
    @Transactional
    public void rejectFriendRequest(Long requestId, Long userId) {
        FriendRequest request = friendRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("好友请求不存在"));
        
        if (!request.getReceiverId().equals(userId)) {
            throw new RuntimeException("无权限操作");
        }
        
        request.setStatus("rejected");
        friendRequestRepository.save(request);
    }
    
    public List<Map<String, Object>> getFriendList(Long userId) {
        List<Friend> friendships = friendRepository.findByUserId(userId);
        
        return friendships.stream().map(friendship -> {
            Long friendId = friendship.getFriendId();
            User friend = userRepository.findById(friendId).orElse(null);
            
            Map<String, Object> friendInfo = new HashMap<>();
            if (friend != null) {
                friendInfo.put("userId", friend.getUserId());
                friendInfo.put("userName", friend.getUserName());
                friendInfo.put("avatar", friend.getAvatar());
                friendInfo.put("userStatus", friend.getUserStatus());
                friendInfo.put("signature", friend.getSignature());
            }
            
            return friendInfo;
        }).collect(Collectors.toList());
    }
    
    @Transactional
    public Message sendMessage(Long senderId, Long receiverId, String content) {
        Message message = new Message();
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setContent(content);
        message.setIfRead(false);
        
        return messageRepository.save(message);
    }
    
    public List<Message> getMessageList(Long userId, Long friendId) {
        return messageRepository.findConversation(userId, friendId);
    }
    
    public Long getUnreadCount(Long userId) {
        return messageRepository.countByReceiverIdAndIfRead(userId, false);
    }

    // 获取按好友分组的未读计数
    public List<UnreadCountDTO> getUnreadCountGroupByFriend(Long userId) {
        return messageRepository.countUnreadByReceiverIdGroupBySenderId(userId);
    }

    // 标记某个好友的消息为已读
    @Transactional
    public void markMessagesAsRead(Long receiverId, Long senderId) {
        // 更新该用户（接收者）收到的、来自某个好友（发送者）的所有未读消息为已读
        messageRepository.updateIfReadByReceiverIdAndSenderId(receiverId, senderId, true);
    }
}
