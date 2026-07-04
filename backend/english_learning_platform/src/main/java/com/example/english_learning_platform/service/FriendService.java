package com.example.english_learning_platform.service;

import com.example.english_learning_platform.dto.FriendRankingDTO;
import com.example.english_learning_platform.dto.UnreadCountDTO;
import com.example.english_learning_platform.entity.FriendRequest;
import com.example.english_learning_platform.entity.Message;
import com.example.english_learning_platform.entity.User;

import java.util.List;
import java.util.Map;

public interface FriendService {

    List<User> searchNewFriends(String keyword, Long currentUserId);

    FriendRequest sendFriendRequest(Long senderId, Long receiverId);

    List<Map<String, Object>> getFriendRequests(Long userId);

    void acceptFriendRequest(Long requestId, Long userId);

    void rejectFriendRequest(Long requestId, Long userId);

    List<Map<String, Object>> getFriendList(Long userId);

    Message sendMessage(Long senderId, Long receiverId, String content);

    List<Message> getMessageList(Long userId, Long friendId);

    Long getUnreadCount(Long userId);

    List<UnreadCountDTO> getUnreadCountGroupByFriend(Long userId);

    void markMessagesAsRead(Long receiverId, Long senderId);

    List<FriendRankingDTO> getFriendWeeklyRanking(Long currentUserId);
}
