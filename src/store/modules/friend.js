// src/store/modules/friend.js
import { defineStore } from 'pinia';
import { friendApi } from '@/api';

export const useFriendStore = defineStore('friend', {
  state: () => ({
    friends: [],
    friendRequests: [],
    initialized: false
  }),
  actions: {
    // 初始化好友数据
    async initFriends() {
      if (this.initialized) return;
      
      try {
        // 并行加载好友列表和请求
        const [friendsResponse, requestsResponse] = await Promise.all([
          friendApi.getFriends(),
          friendApi.getFriendRequests()
        ]);
        
        if (friendsResponse.code === 200) {
          this.friends = friendsResponse.data;
        }
        
        if (requestsResponse.code === 200) {
          this.friendRequests = requestsResponse.data;
        }
        
        this.initialized = true;
      } catch (error) {
        console.error('加载好友数据失败：', error);
      }
    },
    
    // 搜索好友
    async searchFriends(keyword) {
      try {
        const response = await friendApi.searchFriends(keyword);
        if (response.code === 200) {
          return response.data;
        }
      } catch (error) {
        console.error('搜索好友失败：', error);
      }
      return [];
    },
    
    // 添加好友
    async addFriend(friendId) {
      try {
        const response = await friendApi.addFriend(friendId);
        return {
          success: response.code === 200,
          message: response.message
        };
      } catch (error) {
        console.error('添加好友失败：', error);
        return { success: false, message: '添加好友失败，请稍后重试' };
      }
    },
    
    // 接受好友请求
    async acceptFriendRequest(requestId) {
      try {
        const response = await friendApi.acceptFriendRequest(requestId);
        if (response.code === 200) {
          // 从请求列表中移除
          this.friendRequests = this.friendRequests.filter(
            req => req.id !== requestId
          );
          // 添加到好友列表
          this.friends.push(response.data.friend);
          return true;
        }
      } catch (error) {
        console.error('接受好友请求失败：', error);
      }
      return false;
    },
    
    // 拒绝好友请求
    async rejectFriendRequest(requestId) {
      try {
        const response = await friendApi.rejectFriendRequest(requestId);
        if (response.code === 200) {
          // 从请求列表中移除
          this.friendRequests = this.friendRequests.filter(
            req => req.id !== requestId
          );
          return true;
        }
      } catch (error) {
        console.error('拒绝好友请求失败：', error);
      }
      return false;
    }
  }
});