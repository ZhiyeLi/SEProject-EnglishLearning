package com.example.english_learning_platform.service;

import com.example.english_learning_platform.dto.FriendRankingDTO;
import com.example.english_learning_platform.dto.UnreadCountDTO;
import com.example.english_learning_platform.entity.FriendRequest;
import com.example.english_learning_platform.entity.Message;
import com.example.english_learning_platform.entity.User;

import java.util.List;
import java.util.Map;

public interface FriendService {

    /**
     * 搜索可添加的新朋友（排除自己、已好友）
     */
    List<User> searchNewFriends(String keyword, Long currentUserId);

    /**
     * 发送好友申请
     */
    FriendRequest sendFriendRequest(Long senderId, Long receiverId);

    /**
     * 获取收到的待处理好友申请列表
     */
    List<Map<String, Object>> getFriendRequests(Long userId);

    /**
     * 通过好友申请
     */
    void acceptFriendRequest(Long requestId, Long userId);

    /**
     * 拒绝好友申请
     */
    void rejectFriendRequest(Long requestId, Long userId);

    /**
     * 获取好友列表
     */
    List<Map<String, Object>> getFriendList(Long userId);

    /**
     * 给好友发私信
     */
    Message sendMessage(Long senderId, Long receiverId, String content);

    /**
     * 获取与某个好友的聊天记录
     */
    List<Message> getMessageList(Long userId, Long friendId);

    /**
     * 获取全部未读消息总数
     */
    Long getUnreadCount(Long userId);

    /**
     * 按好友分组统计未读消息
     */
    List<UnreadCountDTO> getUnreadCountGroupByFriend(Long userId);

    /**
     * 将指定好友的全部消息标记为已读
     */
    void markMessagesAsRead(Long receiverId, Long senderId);

    /**
     * 获取好友本周学习单词排行榜（包含自己）
     */
    List<FriendRankingDTO> getFriendWeeklyRanking(Long currentUserId);
    /**
     * 删除好友（双向删除）
     */
    void deleteFriend(Long userId, Long friendId);
}