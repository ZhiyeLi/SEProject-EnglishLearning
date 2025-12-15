package com.example.english_learning_platform.repository;

import com.example.english_learning_platform.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 用户数据访问接口
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * 根据用户名查找用户
     */
    Optional<User> findByUserName(String userName);
    
    /**
     * 根据邮箱查找用户
     */
    Optional<User> findByUserEmail(String userEmail);
    
    /**
     * 检查用户名是否存在
     */
    boolean existsByUserName(String userName);
    
    /**
     * 检查邮箱是否存在
     */
    boolean existsByUserEmail(String userEmail);

    /**
     * 根据好友ID列表和关键词（用户ID/用户名）查询好友
     * @param friendIds 好友ID列表
     * @param targetUserId 关键词转成的用户ID（可为null）
     * @param keyword 搜索关键词
     * @return 匹配的用户列表
     */
    @Query("SELECT u FROM User u WHERE u.userId IN :friendIds " +
            "AND ( (:targetUserId IS NOT NULL AND u.userId = :targetUserId) " +
            "OR LOWER(u.userName) LIKE LOWER(CONCAT('%', :keyword, '%')) )")
    List<User> findByFriendIdsAndKeyword(
            @Param("friendIds") List<Long> friendIds,
            @Param("targetUserId") Long targetUserId,
            @Param("keyword") String keyword
    );

    List<User> findByUserNameContainingOrUserEmailContaining(String userName, String userEmail);
}
