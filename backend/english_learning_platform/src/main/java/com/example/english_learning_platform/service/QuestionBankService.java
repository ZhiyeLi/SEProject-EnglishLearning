package com.example.english_learning_platform.service;

import com.example.english_learning_platform.dto.questionbank.*;
import com.example.english_learning_platform.entity.*;
import com.example.english_learning_platform.repository.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 题库服务类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionBankService {
    
    private final ExamPaperRepository examPaperRepository;
    private final QuestionBankRepository questionBankRepository;
    private final QuestionSubItemRepository questionSubItemRepository;
    private final UserFavoriteRepository userFavoriteRepository;
    private final UserExamRecordRepository userExamRecordRepository;
    private final UserAnswerDetailRepository userAnswerDetailRepository;
    private final ObjectMapper objectMapper;
    
    /**
     * 获取列表（考试模式或单题模式）
     */
    @SuppressWarnings("null")
    public QuestionBankListResponse<?> getList(Long userId, QuestionBankListRequest request) {
        if ("exam".equals(request.getMode())) {
            return getExamList(userId, request);
        } else if ("single".equals(request.getMode())) {
            return getSingleList(userId, request);
        }
        throw new IllegalArgumentException("Invalid mode: " + request.getMode());
    }
    
    /**
     * 获取考试模式列表
     */
    private QuestionBankListResponse<ExamPaperItemDTO> getExamList(Long userId, QuestionBankListRequest request) {
        // 构建排序
        Sort sort = buildSort(request.getSortBy());
        Pageable pageable = PageRequest.of(request.getPage() - 1, request.getPageSize(), sort);
        
        // 获取用户的完成状态和收藏状态
        List<Long> completedPaperIds = userExamRecordRepository.findCompletedPaperIdsByUserId(userId);
        List<Long> favoritedPaperIds = userFavoriteRepository.findPaperIdsByUserId(userId);
        
        // 查询试卷
        Page<ExamPaper> paperPage;
        String category = request.getCategory();
        String keyword = request.getKeyword();
        String status = request.getStatus();
        
        // 将 containsSectionType 转换为枚举列表
        List<QuestionBank.SectionType> sectionTypes = null;
        if (request.getContainsSectionType() != null && !request.getContainsSectionType().isEmpty()) {
            sectionTypes = request.getContainsSectionType().stream()
                .map(type -> {
                    try {
                        return QuestionBank.SectionType.valueOf(type.toLowerCase());
                    } catch (IllegalArgumentException e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
                
            if (sectionTypes.isEmpty()) {
                return QuestionBankListResponse.<ExamPaperItemDTO>builder()
                    .items(Collections.emptyList())
                    .total(0L)
                    .page(request.getPage())
                    .pageSize(request.getPageSize())
                    .build();
            }
        }
        
        // 根据状态和其他条件选择不同的查询方法
        if ("done".equals(status)) {
            // 筛选已完成的试卷
            if (completedPaperIds.isEmpty()) {
                paperPage = Page.empty(pageable);
            } else if (sectionTypes != null) {
                // 有题型筛选
                if ("all".equals(category)) {
                    paperPage = examPaperRepository.findByContainingSectionTypesAndCompleted(
                        sectionTypes, completedPaperIds, pageable);
                } else {
                    paperPage = examPaperRepository.findByCategoryAndContainingSectionTypesAndCompleted(
                        category, sectionTypes, completedPaperIds, pageable);
                }
            } else {
                // 无题型筛选
                if ("all".equals(category)) {
                    paperPage = examPaperRepository.findByIdIn(completedPaperIds, pageable);
                } else {
                    paperPage = examPaperRepository.findByCategoryAndIdIn(category, completedPaperIds, pageable);
                }
            }
        } else if ("not_done".equals(status)) {
            // 筛选未完成的试卷
            if (sectionTypes != null) {
                // 有题型筛选
                if ("all".equals(category)) {
                    paperPage = examPaperRepository.findByContainingSectionTypesAndNotCompleted(
                        sectionTypes, completedPaperIds.isEmpty() ? null : completedPaperIds, pageable);
                } else {
                    paperPage = examPaperRepository.findByCategoryAndContainingSectionTypesAndNotCompleted(
                        category, sectionTypes, completedPaperIds.isEmpty() ? null : completedPaperIds, pageable);
                }
            } else {
                // 无题型筛选
                if (completedPaperIds.isEmpty()) {
                    // 没有已完成的，所有都是未完成
                    if ("all".equals(category)) {
                        if (keyword != null && !keyword.isEmpty()) {
                            paperPage = examPaperRepository.findByNameContainingIgnoreCase(keyword, pageable);
                        } else {
                            paperPage = examPaperRepository.findAll(pageable);
                        }
                    } else {
                        if (keyword != null && !keyword.isEmpty()) {
                            paperPage = examPaperRepository.findByCategoryAndNameContainingIgnoreCase(
                                category, keyword, pageable);
                        } else {
                            paperPage = examPaperRepository.findByCategory(category, pageable);
                        }
                    }
                } else {
                    if ("all".equals(category)) {
                        paperPage = examPaperRepository.findByIdNotIn(completedPaperIds, pageable);
                    } else {
                        paperPage = examPaperRepository.findByCategoryAndIdNotIn(category, completedPaperIds, pageable);
                    }
                }
            }
        } else if ("favorited".equals(status)) {
            // 筛选收藏的试卷
            if (favoritedPaperIds.isEmpty()) {
                paperPage = Page.empty(pageable);
            } else if (sectionTypes != null) {
                // 有题型筛选
                if ("all".equals(category)) {
                    paperPage = examPaperRepository.findByContainingSectionTypesAndFavorited(
                        sectionTypes, favoritedPaperIds, pageable);
                } else {
                    paperPage = examPaperRepository.findByCategoryAndContainingSectionTypesAndFavorited(
                        category, sectionTypes, favoritedPaperIds, pageable);
                }
            } else {
                // 无题型筛选
                if ("all".equals(category)) {
                    paperPage = examPaperRepository.findByIdIn(favoritedPaperIds, pageable);
                } else {
                    paperPage = examPaperRepository.findByCategoryAndIdIn(category, favoritedPaperIds, pageable);
                }
            }
        } else {
            // 默认查询所有
            if (sectionTypes != null) {
                if ("all".equals(category)) {
                    paperPage = examPaperRepository.findByContainingSectionTypes(sectionTypes, pageable);
                } else {
                    paperPage = examPaperRepository.findByCategoryAndContainingSectionTypes(
                        category, sectionTypes, pageable);
                }
            } else {
                if ("all".equals(category)) {
                    if (keyword != null && !keyword.isEmpty()) {
                        paperPage = examPaperRepository.findByNameContainingIgnoreCase(keyword, pageable);
                    } else {
                        paperPage = examPaperRepository.findAll(pageable);
                    }
                } else {
                    if (keyword != null && !keyword.isEmpty()) {
                        paperPage = examPaperRepository.findByCategoryAndNameContainingIgnoreCase(
                            category, keyword, pageable);
                    } else {
                        paperPage = examPaperRepository.findByCategory(category, pageable);
                    }
                }
            }
        }
        
        // 转换为 DTO
        List<ExamPaperItemDTO> items = paperPage.getContent().stream()
            .map(paper -> {
                String paperStatus = completedPaperIds.contains(paper.getId()) ? "done" : "not_done";
                
                return ExamPaperItemDTO.builder()
                    .id(paper.getId())
                    .name(paper.getName())
                    .category(paper.getCategory())
                    .year(paper.getYear())
                    .difficulty(paper.getDifficulty())
                    .status(paperStatus)
                    .isFavorited(favoritedPaperIds.contains(paper.getId()))
                    .createdAt(paper.getCreatedAt())
                    .build();
            })
            .collect(Collectors.toList());
        
        return QuestionBankListResponse.<ExamPaperItemDTO>builder()
            .items(items)
            .total(paperPage.getTotalElements())
            .page(request.getPage())
            .pageSize(request.getPageSize())
            .build();
    }
    
    /**
     * 获取单题模式列表
     */
    private QuestionBankListResponse<QuestionItemDTO> getSingleList(Long userId, QuestionBankListRequest request) {
        // 构建排序（单题模式使用 createdAt 字段）
        Sort sort = buildSortForQuestion(request.getSortBy());
        Pageable pageable = PageRequest.of(request.getPage() - 1, request.getPageSize(), sort);
        
        // 获取用户的完成状态和收藏状态（提前获取，用于数据库层面筛选）
        List<Long> completedQuestionIds = userExamRecordRepository.findCompletedQuestionIdsByUserId(userId);
        List<Long> favoritedQuestionIds = userFavoriteRepository.findQuestionIdsByUserId(userId);
        
        // 根据状态筛选条件选择不同的查询方法
        Page<QuestionBank> questionPage;
        String status = request.getStatus();
        
        if ("done".equals(status)) {
            // 筛选已完成的题目
            if (completedQuestionIds.isEmpty()) {
                // 如果没有已完成的题目，返回空结果
                questionPage = Page.empty(pageable);
            } else {
                questionPage = questionBankRepository.findByFiltersAndCompleted(
                    request.getCategory(),
                    request.getSectionType(),
                    request.getKeyword(),
                    completedQuestionIds,
                    pageable
                );
            }
        } else if ("not_done".equals(status)) {
            // 筛选未完成的题目
            questionPage = questionBankRepository.findByFiltersAndNotCompleted(
                request.getCategory(),
                request.getSectionType(),
                request.getKeyword(),
                completedQuestionIds.isEmpty() ? null : completedQuestionIds,
                pageable
            );
        } else if ("favorited".equals(status)) {
            // 筛选收藏的题目
            if (favoritedQuestionIds.isEmpty()) {
                // 如果没有收藏的题目，返回空结果
                questionPage = Page.empty(pageable);
            } else {
                questionPage = questionBankRepository.findByFiltersAndFavorited(
                    request.getCategory(),
                    request.getSectionType(),
                    request.getKeyword(),
                    favoritedQuestionIds,
                    pageable
                );
            }
        } else {
            // 默认查询所有题目
            questionPage = questionBankRepository.findByFilters(
                request.getCategory(),
                request.getSectionType(),
                request.getKeyword(),
                pageable
            );
        }
        
        // 获取所有 paper_id 并查询对应的 ExamPaper
        List<Long> paperIds = questionPage.getContent().stream()
            .map(QuestionBank::getPaperId)
            .distinct()
            .collect(Collectors.toList());
        Map<Long, ExamPaper> paperMap = paperIds.isEmpty() ? 
            Collections.emptyMap() : 
            examPaperRepository.findAllById(paperIds).stream()
                .collect(Collectors.toMap(ExamPaper::getId, p -> p));
        
        // 转换为 DTO
        List<QuestionItemDTO> items = questionPage.getContent().stream()
            .map(question -> {
                ExamPaper paper = paperMap.get(question.getPaperId());
                String questionStatus = completedQuestionIds.contains(question.getId()) ? "done" : "not_done";
                
                return QuestionItemDTO.builder()
                    .id(question.getId())
                    .title(question.getTitle())
                    .sectionType(question.getSectionType().name())
                    .sectionName(question.getSectionName())
                    .paperId(question.getPaperId())
                    .paperName(paper != null ? paper.getName() : "")
                    .paperCategory(paper != null ? paper.getCategory() : "")
                    .status(questionStatus)
                    .isFavorited(favoritedQuestionIds.contains(question.getId()))
                    .createdAt(question.getCreatedAt())
                    .build();
            })
            .collect(Collectors.toList());
        
        return QuestionBankListResponse.<QuestionItemDTO>builder()
            .items(items)
            .total(questionPage.getTotalElements())
            .page(request.getPage())
            .pageSize(request.getPageSize())
            .build();
    }
    
    /**
     * 构建排序（考试模式）
     */
    private Sort buildSort(String sortBy) {
        return switch (sortBy) {
            case "year_desc" -> Sort.by(Sort.Direction.DESC, "year");
            case "year_asc" -> Sort.by(Sort.Direction.ASC, "year");
            case "created_desc" -> Sort.by(Sort.Direction.DESC, "createdAt");
            case "created_asc" -> Sort.by(Sort.Direction.ASC, "createdAt");
            default -> Sort.by(Sort.Direction.DESC, "year");
        };
    }
    
    /**
     * 构建排序（单题模式 - QuestionBank 没有 year 字段，使用 createdAt）
     */
    private Sort buildSortForQuestion(String sortBy) {
        return switch (sortBy) {
            case "year_desc" -> Sort.by(Sort.Direction.DESC, "createdAt"); // 使用创建时间代替年份
            case "year_asc" -> Sort.by(Sort.Direction.ASC, "createdAt");
            case "created_desc" -> Sort.by(Sort.Direction.DESC, "createdAt");
            case "created_asc" -> Sort.by(Sort.Direction.ASC, "createdAt");
            default -> Sort.by(Sort.Direction.DESC, "createdAt");
        };
    }
    
    /**
     * 状态筛选
     */
    private <T> List<T> filterByStatus(List<T> items, String status, 
                                        List<Long> completedIds, List<Long> favoritedIds) {
        if ("all".equals(status)) {
            return items;
        }
        
        return items.stream().filter(item -> {
            if ("done".equals(status)) {
                if (item instanceof ExamPaperItemDTO) {
                    return "done".equals(((ExamPaperItemDTO) item).getStatus());
                } else if (item instanceof QuestionItemDTO) {
                    return "done".equals(((QuestionItemDTO) item).getStatus());
                }
            } else if ("not_done".equals(status)) {
                if (item instanceof ExamPaperItemDTO) {
                    return "not_done".equals(((ExamPaperItemDTO) item).getStatus());
                } else if (item instanceof QuestionItemDTO) {
                    return "not_done".equals(((QuestionItemDTO) item).getStatus());
                }
            } else if ("favorited".equals(status)) {
                if (item instanceof QuestionItemDTO) {
                    return ((QuestionItemDTO) item).getIsFavorited();
                }
            }
            return true;
        }).collect(Collectors.toList());
    }
    
    /**
     * 获取今日统计
     */
    public TodayStatsDTO getTodayStats(Long userId) {
        Long count = userAnswerDetailRepository.countTodayAnswers(userId);
        Long wrongCount = userAnswerDetailRepository.countTodayWrongAnswers(userId);
        
        return TodayStatsDTO.builder()
            .count(count != null ? count : 0L)
            .wrongCount(wrongCount != null ? wrongCount : 0L)
            .build();
    }
    
    /**
     * 添加收藏
     */
    @Transactional
    public void addFavorite(Long userId, FavoriteRequest request) {
        if ("paper".equals(request.getType())) {
            // 试卷收藏
            Long paperId = request.getId();
            if (!userFavoriteRepository.existsByUserIdAndPaperId(userId, paperId)) {
                UserFavorite favorite = new UserFavorite();
                favorite.setUserId(userId);
                favorite.setPaperId(paperId);
                userFavoriteRepository.save(favorite);
            }
        } else if ("question".equals(request.getType())) {
            Long questionId = request.getId();
            if (!userFavoriteRepository.existsByUserIdAndQuestionId(userId, questionId)) {
                UserFavorite favorite = new UserFavorite();
                favorite.setUserId(userId);
                favorite.setQuestionId(questionId);
                userFavoriteRepository.save(favorite);
            }
        } else {
            throw new IllegalArgumentException("Invalid type: " + request.getType());
        }
    }
    
    /**
     * 取消收藏
     */
    @Transactional
    public void removeFavorite(Long userId, FavoriteRequest request) {
        if ("paper".equals(request.getType())) {
            // 取消试卷收藏
            userFavoriteRepository.deleteByUserIdAndPaperId(userId, request.getId());
        } else if ("question".equals(request.getType())) {
            userFavoriteRepository.deleteByUserIdAndQuestionId(userId, request.getId());
        } else {
            throw new IllegalArgumentException("Invalid type: " + request.getType());
        }
    }
    
    /**
     * 获取试卷详情（考试模式）
     */
    @SuppressWarnings("null")
    public ExamPaperDetailDTO getExamPaperDetail(Long userId, Long paperId) {
        // 查询试卷
        ExamPaper paper = examPaperRepository.findById(paperId)
            .orElseThrow(() -> new IllegalArgumentException("Paper not found: " + paperId));
        
        // 查询所有题目
        List<QuestionBank> questions = questionBankRepository.findByPaperIdOrderBySortOrder(paperId);
        
        // 查询所有小题
        List<Long> questionIds = questions.stream().map(QuestionBank::getId).collect(Collectors.toList());
        List<QuestionSubItem> subItems = questionSubItemRepository
            .findByParentQuestionIdInOrderByParentQuestionIdAscSortOrderAsc(questionIds);
        
        // 检查收藏状态
        List<Long> favoritedIds = userFavoriteRepository.findQuestionIdsByUserId(userId);
        boolean isFavorited = questions.stream().anyMatch(q -> favoritedIds.contains(q.getId()));
        
        // 构建 DTO
        ExamPaperDetailDTO.PaperInfo paperInfo = ExamPaperDetailDTO.PaperInfo.builder()
            .id(paper.getId())
            .name(paper.getName())
            .category(paper.getCategory())
            .year(paper.getYear())
            .difficulty(paper.getDifficulty())
            .build();
        
        List<ExamPaperDetailDTO.QuestionDTO> questionDTOs = questions.stream()
            .map(q -> ExamPaperDetailDTO.QuestionDTO.builder()
                .id(q.getId())
                .paperId(q.getPaperId())
                .sectionType(q.getSectionType().name())
                .sectionName(q.getSectionName())
                .title(q.getTitle())
                .materialText(q.getMaterialText())
                .materialImage(q.getMaterialImage())
                .audioUrl(q.getAudioUrl())
                .audioStartSec(q.getAudioStartSec())
                .audioEndSec(q.getAudioEndSec())
                .sortOrder(q.getSortOrder())
                .build())
            .collect(Collectors.toList());
        
        List<ExamPaperDetailDTO.SubItemDTO> subItemDTOs = subItems.stream()
            .map(this::convertToSubItemDTO)
            .collect(Collectors.toList());
        
        return ExamPaperDetailDTO.builder()
            .paper(paperInfo)
            .questions(questionDTOs)
            .subItems(subItemDTOs)
            .isFavorited(isFavorited)
            .build();
    }
    
    /**
     * 获取题目详情（单题模式）
     */
    @SuppressWarnings("null")
    public QuestionDetailDTO getQuestionDetail(Long userId, Long questionId) {
        // 查询题目
        QuestionBank question = questionBankRepository.findById(questionId)
            .orElseThrow(() -> new IllegalArgumentException("Question not found: " + questionId));
        
        // 查询试卷
        ExamPaper paper = examPaperRepository.findById(question.getPaperId())
            .orElseThrow(() -> new IllegalArgumentException("Paper not found: " + question.getPaperId()));
        
        // 查询小题
        List<QuestionSubItem> subItems = questionSubItemRepository
            .findByParentQuestionIdOrderBySortOrder(questionId);
        
        // 检查收藏状态
        boolean isFavorited = userFavoriteRepository.existsByUserIdAndQuestionId(userId, questionId);
        
        // 构建 DTO
        QuestionDetailDTO.QuestionInfo questionInfo = QuestionDetailDTO.QuestionInfo.builder()
            .id(question.getId())
            .paperId(question.getPaperId())
            .sectionType(question.getSectionType().name())
            .sectionName(question.getSectionName())
            .title(question.getTitle())
            .materialText(question.getMaterialText())
            .materialImage(question.getMaterialImage())
            .audioUrl(question.getAudioUrl())
            .audioStartSec(question.getAudioStartSec())
            .audioEndSec(question.getAudioEndSec())
            .category(paper.getCategory())
            .paperName(paper.getName())
            .build();
        
        List<QuestionDetailDTO.SubItemDTO> subItemDTOs = subItems.stream()
            .map(sub -> {
                List<QuestionDetailDTO.OptionDTO> options = parseOptionsForQuestionDetail(sub.getOptions());
                List<String> answer = parseAnswer(sub.getAnswer());
                
                return QuestionDetailDTO.SubItemDTO.builder()
                    .id(sub.getId())
                    .parentQuestionId(sub.getParentQuestionId())
                    .content(sub.getContent())
                    .itemType(sub.getItemType())
                    .options(options)
                    .answer(answer)
                    .explanation(sub.getExplanation())
                    .scoreValue(sub.getScoreValue())
                    .sortOrder(sub.getSortOrder())
                    .build();
            })
            .collect(Collectors.toList());
        
        return QuestionDetailDTO.builder()
            .question(questionInfo)
            .subItems(subItemDTOs)
            .isFavorited(isFavorited)
            .build();
    }
    
    /**
     * 提交答案
     */
    @SuppressWarnings("null")
    @Transactional
    public SubmitAnswerResponse submitAnswer(Long userId, SubmitAnswerRequest request) {
        // 创建或查找考试记录
        UserExamRecord record;
        if ("exam".equals(request.getMode())) {
            record = createOrFindRecord(userId, request.getPaperId(), 
                UserExamRecord.Mode.full_paper, null);
        } else {
            record = createOrFindRecord(userId, request.getPaperId(), 
                UserExamRecord.Mode.single_part, request.getQuestionId());
        }
        
        // 获取所有小题的正确答案
        List<Long> subItemIds = request.getAnswers().stream()
            .map(SubmitAnswerRequest.AnswerItem::getSubItemId)
            .collect(Collectors.toList());
        Map<Long, QuestionSubItem> subItemMap = questionSubItemRepository.findAllById(subItemIds)
            .stream()
            .collect(Collectors.toMap(QuestionSubItem::getId, s -> s));
        
        // 批量获取父题目信息（用于显示材料原文）
        Set<Long> parentQuestionIds = subItemMap.values().stream()
            .map(QuestionSubItem::getParentQuestionId)
            .collect(Collectors.toSet());
        Map<Long, QuestionBank> parentQuestionMap = questionBankRepository.findAllById(parentQuestionIds)
            .stream()
            .collect(Collectors.toMap(QuestionBank::getId, q -> q));
        
        // 批量处理答案
        List<SubmitAnswerResponse.AnswerDetail> details = new ArrayList<>();
        BigDecimal totalScore = BigDecimal.ZERO;
        int correctCount = 0;
        int objectiveCount = 0; // 客观题计数
        
        for (SubmitAnswerRequest.AnswerItem answerItem : request.getAnswers()) {
            QuestionSubItem subItem = subItemMap.get(answerItem.getSubItemId());
            if (subItem == null) continue;
            
            String itemType = subItem.getItemType();
            // 判断是否为客观题（可自动判分）
            boolean isObjective = isObjectiveQuestion(itemType);
            
            // 解析正确答案
            List<String> correctAnswer = parseAnswer(subItem.getAnswer());
            
            // 校验答案（仅对客观题判断对错）
            Boolean isCorrect = null;
            BigDecimal scoreObtained = BigDecimal.ZERO;
            
            if (isObjective) {
                objectiveCount++;
                isCorrect = checkAnswer(answerItem.getAnswer(), correctAnswer, itemType);
                if (isCorrect) {
                    correctCount++;
                    scoreObtained = subItem.getScoreValue();
                }
            }
            totalScore = totalScore.add(scoreObtained);
            
            // 保存答题详情
            UserAnswerDetail detail = new UserAnswerDetail();
            detail.setRecordId(record.getId());
            detail.setSubItemId(subItem.getId());
            detail.setUserId(userId);
            detail.setUserContent(convertAnswerToJson(answerItem.getAnswer()));
            detail.setIsCorrect(isObjective ? (isCorrect ? 1 : 0) : -1); // -1 表示主观题
            detail.setScoreObtained(scoreObtained);
            userAnswerDetailRepository.save(detail);
            
            // 解析选项
            List<SubmitAnswerResponse.OptionItem> optionItems = null;
            if (subItem.getOptions() != null && !subItem.getOptions().isEmpty()) {
                try {
                    optionItems = parseOptionsForResponse(subItem.getOptions());
                } catch (Exception e) {
                    // 忽略解析错误
                }
            }
            
            // 获取父题目信息
            QuestionBank parentQuestion = parentQuestionMap.get(subItem.getParentQuestionId());
            String materialText = parentQuestion != null ? parentQuestion.getMaterialText() : null;
            String materialImage = parentQuestion != null ? parentQuestion.getMaterialImage() : null;
            String audioUrl = parentQuestion != null ? parentQuestion.getAudioUrl() : null;
            Integer audioStartSec = parentQuestion != null ? parentQuestion.getAudioStartSec() : null;
            Integer audioEndSec = parentQuestion != null ? parentQuestion.getAudioEndSec() : null;
            
            // 构建详情 DTO
            details.add(SubmitAnswerResponse.AnswerDetail.builder()
                .subItemId(subItem.getId())
                .parentQuestionId(subItem.getParentQuestionId())
                .materialText(materialText)
                .materialImage(materialImage)
                .audioUrl(audioUrl)
                .audioStartSec(audioStartSec)
                .audioEndSec(audioEndSec)
                .itemType(itemType)
                .content(subItem.getContent())
                .userAnswer(answerItem.getAnswer())
                .correctAnswer(correctAnswer)
                .isCorrect(isCorrect)
                .isObjective(isObjective)
                .scoreObtained(scoreObtained)
                .explanation(subItem.getExplanation())
                .options(optionItems)
                .build());
        }
        
        // 更新考试记录
        record.setStatus(UserExamRecord.Status.completed);
        record.setTotalScore(totalScore);
        record.setCompletedAt(LocalDateTime.now());
        userExamRecordRepository.save(record);
        
        // 计算正确率（仅针对客观题）
        int accuracy = objectiveCount == 0 ? 0 : 
            (int) Math.round((double) correctCount / objectiveCount * 100);
        
        // 计算用时（秒）
        long duration = 0;
        if (record.getStartedAt() != null && record.getCompletedAt() != null) {
            duration = java.time.Duration.between(record.getStartedAt(), record.getCompletedAt()).getSeconds();
        }
        
        return SubmitAnswerResponse.builder()
            .recordId(record.getId())
            .score(totalScore)
            .accuracy(accuracy)
            .totalQuestions(request.getAnswers().size())
            .correctCount(correctCount)
            .objectiveCount(objectiveCount)
            .duration(duration)
            .details(details)
            .build();
    }
    
    /**
     * 判断是否为客观题（可自动判分）
     */
    private boolean isObjectiveQuestion(String itemType) {
        if (itemType == null) return false;
        return switch (itemType.toLowerCase()) {
            case "choice", "multi_choice", "multiple", "insert", "blank", "fill", "gap_fill" -> true;
            default -> false; // essay, writing, speaking 等为主观题
        };
    }
    
    /**
     * 解析选项为响应格式
     */
    @SuppressWarnings("unchecked")
    private List<SubmitAnswerResponse.OptionItem> parseOptionsForResponse(String optionsJson) {
        if (optionsJson == null || optionsJson.isEmpty() || "[]".equals(optionsJson)) {
            return null;
        }
        try {
            List<Map<String, String>> optionsList = objectMapper.readValue(optionsJson, List.class);
            return optionsList.stream()
                .map(opt -> SubmitAnswerResponse.OptionItem.builder()
                    .key((String) opt.get("key"))
                    .value((String) opt.get("value"))
                    .build())
                .collect(Collectors.toList());
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 获取错题列表
     */
    @SuppressWarnings("null")
    public WrongQuestionsResponse getWrongQuestions(Long userId, String category, 
                                                     String sectionType, String sortBy) {
        // 查询所有错题
        List<UserAnswerDetail> wrongAnswers = userAnswerDetailRepository.findWrongAnswersByUserId(userId);
        
        // 查询近7天错题
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        List<UserAnswerDetail> recentWrongAnswers = userAnswerDetailRepository
            .findRecentWrongAnswers(userId, sevenDaysAgo);
        
        // 计算整体正确率
        Double accuracyDouble = userAnswerDetailRepository.calculateTodayAccuracy(userId);
        Integer accuracy = accuracyDouble != null ? (int) Math.round(accuracyDouble * 100) : 0;
        
        // 统计信息
        WrongQuestionsResponse.StatsDTO stats = WrongQuestionsResponse.StatsDTO.builder()
            .total((long) wrongAnswers.size())
            .recent((long) recentWrongAnswers.size())
            .accuracy(accuracy)
            .build();
        
        // 按 parent_question_id 分组统计
        Map<Long, List<UserAnswerDetail>> groupedByQuestion = wrongAnswers.stream()
            .collect(Collectors.groupingBy(UserAnswerDetail::getSubItemId));
        
        // 查询所有相关的小题、题目和试卷
        List<Long> subItemIds = new ArrayList<>(groupedByQuestion.keySet());
        Map<Long, QuestionSubItem> subItemMap = questionSubItemRepository.findAllById(subItemIds)
            .stream()
            .collect(Collectors.toMap(QuestionSubItem::getId, s -> s));
        
        List<Long> questionIds = subItemMap.values().stream()
            .map(QuestionSubItem::getParentQuestionId)
            .distinct()
            .collect(Collectors.toList());
        Map<Long, QuestionBank> questionMap = questionBankRepository.findAllById(questionIds)
            .stream()
            .collect(Collectors.toMap(QuestionBank::getId, q -> q));
        
        List<Long> paperIds = questionMap.values().stream()
            .map(QuestionBank::getPaperId)
            .distinct()
            .collect(Collectors.toList());
        Map<Long, ExamPaper> paperMap = examPaperRepository.findAllById(paperIds)
            .stream()
            .collect(Collectors.toMap(ExamPaper::getId, p -> p));
        
        // 构建错题列表
        List<WrongQuestionsResponse.WrongQuestionItem> items = new ArrayList<>();
        
        for (Map.Entry<Long, List<UserAnswerDetail>> entry : groupedByQuestion.entrySet()) {
            Long subItemId = entry.getKey();
            List<UserAnswerDetail> details = entry.getValue();
            
            QuestionSubItem subItem = subItemMap.get(subItemId);
            if (subItem == null) continue;
            
            QuestionBank question = questionMap.get(subItem.getParentQuestionId());
            if (question == null) continue;
            
            ExamPaper paper = paperMap.get(question.getPaperId());
            if (paper == null) continue;
            
            // 筛选
            if (!"all".equals(category) && !paper.getCategory().equals(category)) continue;
            if (!"all".equals(sectionType) && !question.getSectionType().name().equals(sectionType)) continue;
            
            // 获取预览文本
            String preview = subItem.getContent();
            if (preview != null && preview.length() > 50) {
                preview = preview.substring(0, 50) + "...";
            }
            
            // 获取最后错误时间
            LocalDateTime lastWrongDate = details.stream()
                .map(UserAnswerDetail::getCreatedAt)
                .max(LocalDateTime::compareTo)
                .orElse(null);
            
            items.add(WrongQuestionsResponse.WrongQuestionItem.builder()
                .id(details.get(0).getId())
                .questionId(question.getId())
                .paperName(paper.getName())
                .sectionName(question.getSectionName())
                .sectionType(question.getSectionType().name())
                .category(paper.getCategory())
                .preview(preview)
                .wrongCount((long) details.size())
                .lastWrongDate(lastWrongDate)
                .build());
        }
        
        // 排序
        if ("recent".equals(sortBy)) {
            items.sort(Comparator.comparing(WrongQuestionsResponse.WrongQuestionItem::getLastWrongDate).reversed());
        } else if ("frequency".equals(sortBy)) {
            items.sort(Comparator.comparing(WrongQuestionsResponse.WrongQuestionItem::getWrongCount).reversed());
        }
        
        return WrongQuestionsResponse.builder()
            .stats(stats)
            .items(items)
            .total((long) items.size())
            .build();
    }
    
    // ==================== 辅助方法 ====================
    
    /**
     * 创建或查找考试记录
     */
    private UserExamRecord createOrFindRecord(Long userId, Long paperId, 
                                              UserExamRecord.Mode mode, Long targetQuestionId) {
        Optional<UserExamRecord> existing = userExamRecordRepository
            .findByUserIdAndPaperIdAndModeAndStatusAndTargetQuestionId(
                userId, paperId, mode, UserExamRecord.Status.ongoing, targetQuestionId);
        
        if (existing.isPresent()) {
            return existing.get();
        }
        
        UserExamRecord record = new UserExamRecord();
        record.setUserId(userId);
        record.setPaperId(paperId);
        record.setMode(mode);
        record.setTargetQuestionId(targetQuestionId);
        record.setStatus(UserExamRecord.Status.ongoing);
        return userExamRecordRepository.save(record);
    }
    
    /**
     * 转换为 SubItemDTO
     */
    private ExamPaperDetailDTO.SubItemDTO convertToSubItemDTO(QuestionSubItem subItem) {
        List<ExamPaperDetailDTO.OptionDTO> options = parseOptions(subItem.getOptions());
        List<String> answer = parseAnswer(subItem.getAnswer());
        
        return ExamPaperDetailDTO.SubItemDTO.builder()
            .id(subItem.getId())
            .parentQuestionId(subItem.getParentQuestionId())
            .content(subItem.getContent())
            .itemType(subItem.getItemType())
            .options(options)
            .answer(answer)
            .explanation(subItem.getExplanation())
            .scoreValue(subItem.getScoreValue())
            .sortOrder(subItem.getSortOrder())
            .build();
    }
    
    /**
     * 解析选项 JSON
     */
    private List<ExamPaperDetailDTO.OptionDTO> parseOptions(String optionsJson) {
        if (optionsJson == null || optionsJson.isEmpty()) {
            return new ArrayList<>();
        }
        
        try {
            List<Map<String, String>> optionList = objectMapper.readValue(
                optionsJson, new TypeReference<List<Map<String, String>>>() {});
            
            return optionList.stream()
                .map(map -> ExamPaperDetailDTO.OptionDTO.builder()
                    .key(map.get("key"))
                    .value(map.get("value"))
                    .build())
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to parse options JSON: {}", optionsJson, e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 解析选项 JSON（用于 QuestionDetailDTO）
     */
    private List<QuestionDetailDTO.OptionDTO> parseOptionsForQuestionDetail(String optionsJson) {
        if (optionsJson == null || optionsJson.isEmpty()) {
            return new ArrayList<>();
        }
        
        try {
            List<Map<String, String>> optionList = objectMapper.readValue(
                optionsJson, new TypeReference<List<Map<String, String>>>() {});
            
            return optionList.stream()
                .map(map -> QuestionDetailDTO.OptionDTO.builder()
                    .key(map.get("key"))
                    .value(map.get("value"))
                    .build())
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to parse options JSON: {}", optionsJson, e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 解析答案 JSON
     */
    private List<String> parseAnswer(String answerJson) {
        if (answerJson == null || answerJson.isEmpty()) {
            return new ArrayList<>();
        }
        
        try {
            return objectMapper.readValue(answerJson, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            log.error("Failed to parse answer JSON: {}", answerJson, e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 校验答案
     */
    private boolean checkAnswer(Object userAnswer, List<String> correctAnswer, String itemType) {
        if ("text".equals(itemType)) {
            // 文本题暂时标记为错误，待人工评分
            return false;
        }
        
        List<String> userAnswerList = new ArrayList<>();
        if (userAnswer instanceof String) {
            userAnswerList.add(((String) userAnswer).toUpperCase());
        } else if (userAnswer instanceof List) {
            @SuppressWarnings("unchecked")
            List<String> list = (List<String>) userAnswer;
            userAnswerList = list.stream()
                .map(String::toUpperCase)
                .sorted()
                .collect(Collectors.toList());
        }
        
        List<String> correctAnswerUpper = correctAnswer.stream()
            .map(String::toUpperCase)
            .sorted()
            .collect(Collectors.toList());
        
        return userAnswerList.equals(correctAnswerUpper);
    }
    
    /**
     * 将答案转换为 JSON 字符串
     */
    private String convertAnswerToJson(Object answer) {
        try {
            return objectMapper.writeValueAsString(answer);
        } catch (Exception e) {
            log.error("Failed to convert answer to JSON: {}", answer, e);
            return answer != null ? answer.toString() : "";
        }
    }
}
