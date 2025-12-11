package com.example.english_learning_platform.repository;

import com.example.english_learning_platform.entity.UserWordProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserWordProgressRepository extends JpaRepository<UserWordProgress, Long> {
    List<UserWordProgress> findByUserIdAndTypeId(Long userId, Long typeId);
    Optional<UserWordProgress> findByUserIdAndWordId(Long userId, Long wordId);
    Long countByUserIdAndPassedDateIsNotNull(Long userId);
    Long countByUserIdAndTypeIdAndPassedDateIsNotNull(Long userId, Long typeId);
    List<UserWordProgress> findByUserIdAndPassedDateIsNotNull(Long userId);
    List<UserWordProgress> findByUserIdAndPassedDate(Long userId, LocalDate passedDate);
}
