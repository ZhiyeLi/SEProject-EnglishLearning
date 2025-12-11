package com.example.english_learning_platform.repository;

import com.example.english_learning_platform.entity.Friend;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface FriendRepository extends JpaRepository<Friend, Long> {
    List<Friend> findByUserId(Long userId);
    
    @Query("SELECT f FROM Friend f WHERE (f.userId = ?1 AND f.friendId = ?2) OR (f.userId = ?2 AND f.friendId = ?1)")
    Optional<Friend> findFriendship(Long userId1, Long userId2);
    
    boolean existsByUserIdAndFriendId(Long userId, Long friendId);
}
