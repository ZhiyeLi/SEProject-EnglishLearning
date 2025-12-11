package com.example.english_learning_platform.repository;

import com.example.english_learning_platform.entity.FriendRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {
    List<FriendRequest> findByReceiverIdAndStatus(Long receiverId, String status);
    List<FriendRequest> findBySenderIdAndStatus(Long senderId, String status);
    Optional<FriendRequest> findBySenderIdAndReceiverIdAndStatus(Long senderId, Long receiverId, String status);
    boolean existsBySenderIdAndReceiverIdAndStatus(Long senderId, Long receiverId, String status);
}
