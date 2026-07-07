package com.example.english_learning_platform.repository;

import com.example.english_learning_platform.entity.UserCourseFavorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserCourseFavoriteRepository extends JpaRepository<UserCourseFavorite, Long> {

    Optional<UserCourseFavorite> findByUserIdAndCourseId(Long userId, Long courseId);

    List<UserCourseFavorite> findByUserId(Long userId);

    void deleteByUserIdAndCourseId(Long userId, Long courseId);
}
