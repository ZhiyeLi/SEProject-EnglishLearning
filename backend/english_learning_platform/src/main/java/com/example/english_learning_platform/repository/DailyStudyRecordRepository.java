package com.example.english_learning_platform.repository;

import com.example.english_learning_platform.entity.DailyStudyRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DailyStudyRecordRepository extends JpaRepository<DailyStudyRecord, Long> {
    Optional<DailyStudyRecord> findByUserIdAndStudyDate(Long userId, LocalDate studyDate);
    List<DailyStudyRecord> findByUserIdOrderByStudyDateDesc(Long userId);
    List<DailyStudyRecord> findByUserIdAndStudyDateBetweenOrderByStudyDate(Long userId, LocalDate startDate, LocalDate endDate);
}
