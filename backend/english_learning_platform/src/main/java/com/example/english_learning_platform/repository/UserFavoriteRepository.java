package com.example.english_learning_platform.repository;

import com.example.english_learning_platform.entity.UserFavorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserFavoriteRepository extends JpaRepository<UserFavorite, Long> {
    
    // 查找用户对某个题目的收藏
    Optional<UserFavorite> findByUserIdAndQuestionId(Long userId, Long questionId);
    
    // 检查用户是否收藏了某个题目
    boolean existsByUserIdAndQuestionId(Long userId, Long questionId);

    // 查找用户对某个试卷的收藏
    Optional<UserFavorite> findByUserIdAndPaperId(Long userId, Long paperId);

    // 检查用户是否收藏了某个试卷
    boolean existsByUserIdAndPaperId(Long userId, Long paperId);
    
    // 查询用户收藏的所有题目ID
    @Query("SELECT uf.questionId FROM UserFavorite uf WHERE uf.userId = :userId AND uf.questionId IS NOT NULL")
    List<Long> findQuestionIdsByUserId(@Param("userId") Long userId);

    // 查询用户收藏的所有试卷ID
    @Query("SELECT uf.paperId FROM UserFavorite uf WHERE uf.userId = :userId AND uf.paperId IS NOT NULL")
    List<Long> findPaperIdsByUserId(@Param("userId") Long userId);
    
    // 删除用户收藏
    void deleteByUserIdAndQuestionId(Long userId, Long questionId);

    // 删除用户对试卷的收藏
    void deleteByUserIdAndPaperId(Long userId, Long paperId);
}
