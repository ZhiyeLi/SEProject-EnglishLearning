package com.example.english_learning_platform.service;

import com.example.english_learning_platform.dto.UnreadCountDTO;
import com.example.english_learning_platform.entity.*;
import com.example.english_learning_platform.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import com.example.english_learning_platform.dto.FriendRankingDTO;
import com.example.english_learning_platform.repository.UserWordProgressRepository;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class FriendService {

    private static final Logger logger = LoggerFactory.getLogger(FriendService.class);

    private final UserRepository userRepository;
    private final FriendRepository friendRepository;
    private final FriendRequestRepository friendRequestRepository;
    private final MessageRepository messageRepository;

    private final UserWordProgressRepository userWordProgressRepository;

    public FriendService(UserRepository userRepository,
                        FriendRepository friendRepository,
                        FriendRequestRepository friendRequestRepository,
                        MessageRepository messageRepository,
                         UserWordProgressRepository userWordProgressRepository) {
        this.userRepository = userRepository;
        this.friendRepository = friendRepository;
        this.friendRequestRepository = friendRequestRepository;
        this.messageRepository = messageRepository;
        this.userWordProgressRepository = userWordProgressRepository;
    }

    public List<User> searchNewFriends(String keyword, Long currentUserId) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return Collections.emptyList();
        }

        // 先查出所有符合关键词的用户 (包含自己和好友)
        List<User> allMatches = userRepository.findByUserNameContainingOrUserEmailContaining(keyword, keyword);

        // 查出当前用户的所有好友 ID（支持双向关系）
        Set<Long> myFriendIds = new HashSet<>();
        List<Long> friendIdsFromRepo = friendRepository.findAllFriendIdsByUserId(currentUserId);
        if (friendIdsFromRepo != null) {
            myFriendIds.addAll(friendIdsFromRepo);
        }

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
        // 防止自己给自己发送请求
        if (senderId.equals(receiverId)) {
            throw new RuntimeException("不能给自己发送好友请求");
        }

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
    
    public List<Map<String, Object>> getFriendRequests(Long userId) {
        // 查询待处理的好友请求
        List<FriendRequest> requests = friendRequestRepository.findByReceiverIdAndStatus(userId, "pending");
        if (requests == null || requests.isEmpty()) {
            return Collections.emptyList();
        }

        // 收集所有发送者ID，批量查询用户信息，避免 N+1
        Set<Long> senderIds = new HashSet<>();
        for (FriendRequest req : requests) {
            senderIds.add(req.getSenderId());
        }

        List<User> senders = userRepository.findAllById(new ArrayList<>(senderIds));
        Map<Long, User> senderMap = senders.stream().collect(Collectors.toMap(User::getUserId, u -> u));

        List<Map<String, Object>> result = new ArrayList<>();
        for (FriendRequest req : requests) {
            Map<String, Object> item = new HashMap<>();
            item.put("requestId", req.getRequestId());
            item.put("senderId", req.getSenderId());
            item.put("receiverId", req.getReceiverId());
            item.put("status", req.getStatus());
            item.put("createdAt", req.getCreatedAt());

            User sender = senderMap.get(req.getSenderId());
            if (sender != null) {
                item.put("userName", sender.getUserName());
                item.put("avatar", sender.getAvatar());
                item.put("signature", sender.getSignature());
            } else {
                item.put("userName", null);
                item.put("avatar", null);
                item.put("signature", null);
            }

            result.add(item);
        }

        return result;
    }
    
    @Transactional
    public void acceptFriendRequest(Long requestId, Long userId) {
        FriendRequest request = friendRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("好友请求不存在"));
        
        if (!request.getReceiverId().equals(userId)) {
            throw new RuntimeException("无权限操作");
        }

        // 建立正向关系：申请人 -> 我（若不存在）
        try {
            if (!friendRepository.existsByUserIdAndFriendId(request.getSenderId(), request.getReceiverId())) {
                Friend forwardShip = new Friend();
                forwardShip.setUserId(request.getSenderId());
                forwardShip.setFriendId(request.getReceiverId());
                friendRepository.save(forwardShip);
            }
        } catch (DataIntegrityViolationException e) {
            // 并发场景下可能出现唯一约束冲突，记录并继续
            logger.warn("忽略正向好友插入的唯一约束冲突: {} -> {}", request.getSenderId(), request.getReceiverId());
        }

        // 建立反向关系：我 -> 申请人（若不存在）
        try {
            if (!friendRepository.existsByUserIdAndFriendId(request.getReceiverId(), request.getSenderId())) {
                Friend backwardShip = new Friend();
                backwardShip.setUserId(request.getReceiverId());
                backwardShip.setFriendId(request.getSenderId());
                friendRepository.save(backwardShip);
            }
        } catch (DataIntegrityViolationException e) {
            logger.warn("忽略反向好友插入的唯一约束冲突: {} -> {}", request.getReceiverId(), request.getSenderId());
        }

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
        // 支持双向好友关系：使用 findAllFriendIdsByUserId
        List<Long> friendIds = friendRepository.findAllFriendIdsByUserId(userId);
        // 使用公共去重/过滤方法
        List<Long> uniqueFriendIds = dedupeAndFilterFriendIds(userId, friendIds);
        if (uniqueFriendIds.isEmpty()) {
            return Collections.emptyList();
        }

        // 批量查询用户，避免 N+1
        List<User> users = userRepository.findAllById(uniqueFriendIds);
        Map<Long, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getUserId, u -> u));

        List<Map<String, Object>> result = new ArrayList<>();
        for (Long fid : uniqueFriendIds) {
            User friend = userMap.get(fid);
            if (friend == null) continue;
            Map<String, Object> friendInfo = new HashMap<>();
            friendInfo.put("userId", friend.getUserId());
            friendInfo.put("userName", friend.getUserName());
            friendInfo.put("avatar", friend.getAvatar());
            friendInfo.put("userStatus", friend.getUserStatus());
            friendInfo.put("signature", friend.getSignature());
            result.add(friendInfo);
        }

        return result;
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

    // 新增：获取好友周学习单词排行榜
    public List<FriendRankingDTO> getFriendWeeklyRanking(Long currentUserId) {
        // 1. 计算本周时间范围：周一00:00 至 周日23:59:59
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime monday = now.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
                .withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime sunday = now.with(TemporalAdjusters.nextOrSame(java.time.DayOfWeek.SUNDAY))
                .withHour(23).withMinute(59).withSecond(59).withNano(999999999);

        // 2. 获取当前用户的所有好友（支持双向关系）
        List<Long> friendIds = friendRepository.findAllFriendIdsByUserId(currentUserId);
        // 使用公共去重/过滤方法
        List<Long> uniqueFriendIds = dedupeAndFilterFriendIds(currentUserId, friendIds);
        // 将当前用户也加入排名（用户自己也可参与排行榜）
        if (uniqueFriendIds == null || uniqueFriendIds.isEmpty()) {
            uniqueFriendIds = new ArrayList<>();
            uniqueFriendIds.add(currentUserId);
        } else if (!uniqueFriendIds.contains(currentUserId)) {
            // 保持当前好友顺序的同时把自己放到首位，确保在结果中可见
            uniqueFriendIds.add(0, currentUserId);
        }
         if (uniqueFriendIds.isEmpty()) {
             return new ArrayList<>(); // 无好友返回空列表
         }

        // 批量获取好友用户信息，避免 N+1
        List<User> friendUsers = userRepository.findAllById(uniqueFriendIds);
        Map<Long, User> friendUserMap = friendUsers.stream()
                .collect(Collectors.toMap(User::getUserId, u -> u));

        // 3. 批量统计每个好友的本周去重单词数，避免对每个好友单独查询
        List<Object[]> counts = userWordProgressRepository.countDistinctWordsByUserIdsAndTimeRange(uniqueFriendIds, monday, sunday);
        Map<Long, Integer> countMap = new HashMap<>();
        if (counts != null) {
            for (Object[] row : counts) {
                if (row == null || row.length < 2) continue;
                Object idObj = row[0];
                Object cntObj = row[1];
                Long uid = null;
                if (idObj instanceof Number) uid = ((Number) idObj).longValue();
                if (uid == null) continue;
                Integer cnt = 0;
                if (cntObj instanceof Number) cnt = ((Number) cntObj).intValue();
                countMap.put(uid, cnt);
            }
        }

        // 4. 遍历去重后的好友，使用 countMap 填充统计数据
        List<FriendRankingDTO> rankingList = new ArrayList<>();
        for (Long friendId : uniqueFriendIds) {
            User friendUser = friendUserMap.get(friendId);
            if (friendUser == null) continue; // 好友不存在则跳过
            Integer totalWords = countMap.getOrDefault(friendId, 0);
            FriendRankingDTO dto = new FriendRankingDTO();
            dto.setUserId(friendId);
            dto.setAvatar(friendUser.getAvatar());
            dto.setUsername(friendUser.getUserName());
            dto.setTotalWords(totalWords);
            rankingList.add(dto);
        }

        // 额外防护：再次按 userId 去重（并保留 totalWords 较大的那一项），以防意外重复
        Map<Long, FriendRankingDTO> dedupMap = new LinkedHashMap<>();
        for (FriendRankingDTO dto : rankingList) {
            Long uid = dto.getUserId();
            if (uid == null) continue;
            if (!dedupMap.containsKey(uid)) {
                dedupMap.put(uid, dto);
            } else {
                FriendRankingDTO existing = dedupMap.get(uid);
                if (dto.getTotalWords() != null && existing.getTotalWords() != null) {
                    if (dto.getTotalWords() > existing.getTotalWords()) {
                        dedupMap.put(uid, dto);
                    }
                }
            }
        }

        List<FriendRankingDTO> finalList = new ArrayList<>(dedupMap.values());

        // 4. 排序：按单词数降序，数量相同则按用户名升序
        finalList.sort((a, b) -> {
            if (b.getTotalWords().equals(a.getTotalWords())) {
                return a.getUsername().compareTo(b.getUsername());
            }
            return b.getTotalWords().compareTo(a.getTotalWords());
        });

        return finalList;
    }

    // 新增公共方法：去重并过滤 null 与自身 ID，保持原始顺序
    private List<Long> dedupeAndFilterFriendIds(Long selfUserId, List<Long> friendIds) {
        if (friendIds == null || friendIds.isEmpty()) {
            return Collections.emptyList();
        }
        Set<Long> unique = new LinkedHashSet<>();
        for (Long id : friendIds) {
            if (id != null && !id.equals(selfUserId)) {
                unique.add(id);
            }
        }
        return new ArrayList<>(unique);
    }
}
