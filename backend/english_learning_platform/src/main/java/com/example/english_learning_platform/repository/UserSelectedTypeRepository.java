package com.example.english_learning_platform.repository;

import com.example.english_learning_platform.entity.UserSelectedType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserSelectedTypeRepository extends JpaRepository<UserSelectedType, Long> {
    Optional<UserSelectedType> findTopByUserIdOrderBySelectedDateDesc(Long userId);
    void deleteByUserId(Long userId);
}
