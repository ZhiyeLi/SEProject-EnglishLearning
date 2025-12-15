package com.example.english_learning_platform.repository;

import com.example.english_learning_platform.entity.Friend;
import com.example.english_learning_platform.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface FriendRepository extends JpaRepository<Friend, Long> {
    List<Friend> findByUserId(Long userId);
    
    @Query("SELECT f FROM Friend f WHERE (f.userId = ?1 AND f.friendId = ?2) OR (f.userId = ?2 AND f.friendId = ?1)")
    Optional<Friend> findFriendship(Long userId1, Long userId2);
    
    boolean existsByUserIdAndFriendId(Long userId, Long friendId);

    /**
     * 查询当前用户的所有好友ID（双向：当前用户是userId 或 friendId）
     * 增强健壮性，避免漏查好友
     */
    @Query("SELECT CASE WHEN f.userId = :userId THEN f.friendId ELSE f.userId END " +
            "FROM Friend f WHERE f.userId = :userId OR f.friendId = :userId")
    List<Long> findAllFriendIdsByUserId(@Param("userId") Long userId);

}
