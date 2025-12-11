package com.example.english_learning_platform.service;

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
    
    public List<User> searchFriend(String keyword) {
        return userRepository.findAll().stream()
                .filter(user -> user.getUserName().contains(keyword) || 
                               user.getUserEmail().contains(keyword))
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
        
        // 创建好友关系
        Friend friend = new Friend();
        friend.setUserId(request.getSenderId());
        friend.setFriendId(request.getReceiverId());
        friendRepository.save(friend);
        
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
}
