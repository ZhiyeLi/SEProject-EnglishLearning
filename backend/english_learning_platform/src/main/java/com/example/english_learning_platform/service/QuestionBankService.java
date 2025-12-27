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
        
        // 查询试卷
        Page<ExamPaper> paperPage;
        String category = request.getCategory();
        String keyword = request.getKeyword();
        
        // 如果有包含题型筛选
        if (request.getContainsSectionType() != null && !request.getContainsSectionType().isEmpty()) {
            if ("all".equals(category)) {
                paperPage = examPaperRepository.findByContainingSectionTypes(
                    request.getContainsSectionType(), pageable);
            } else {
                paperPage = examPaperRepository.findByCategoryAndContainingSectionTypes(
                    category, request.getContainsSectionType(), pageable);
            }
        } else {
            // 普通查询
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
        
        // 获取用户的完成状态和收藏状态
        List<Long> completedPaperIds = userExamRecordRepository.findCompletedPaperIdsByUserId(userId);
        List<Long> favoritedQuestionIds = userFavoriteRepository.findQuestionIdsByUserId(userId);
        
        // 转换为 DTO
        List<ExamPaperItemDTO> items = paperPage.getContent().stream()
            .map(paper -> {
                String status = "not_done";
                if (completedPaperIds.contains(paper.getId())) {
                    status = "done";
                }
                
                return ExamPaperItemDTO.builder()
                    .id(paper.getId())
                    .name(paper.getName())
                    .category(paper.getCategory())
                    .year(paper.getYear())
                    .difficulty(paper.getDifficulty())
                    .status(status)
                    .isFavorited(false) // 试卷不支持收藏，这里固定为false
                    .createdAt(paper.getCreatedAt())
                    .build();
            })
            .collect(Collectors.toList());
        
        // 状态筛选
        items = filterByStatus(items, request.getStatus(), completedPaperIds, favoritedQuestionIds);
        
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
        
        // 查询题目
        Page<QuestionBank> questionPage = questionBankRepository.findByFilters(
            request.getCategory(),
            request.getSectionType(),
            request.getKeyword(),
            pageable
        );
        
        // 获取所有 paper_id 并查询对应的 ExamPaper
        List<Long> paperIds = questionPage.getContent().stream()
            .map(QuestionBank::getPaperId)
            .distinct()
            .collect(Collectors.toList());
        Map<Long, ExamPaper> paperMap = examPaperRepository.findAllById(paperIds).stream()
            .collect(Collectors.toMap(ExamPaper::getId, p -> p));
        
        // 获取用户的完成状态和收藏状态
        List<Long> completedQuestionIds = userExamRecordRepository.findCompletedQuestionIdsByUserId(userId);
        List<Long> favoritedQuestionIds = userFavoriteRepository.findQuestionIdsByUserId(userId);
        
        // 转换为 DTO
        List<QuestionItemDTO> items = questionPage.getContent().stream()
            .map(question -> {
                ExamPaper paper = paperMap.get(question.getPaperId());
                String status = completedQuestionIds.contains(question.getId()) ? "done" : "not_done";
                
                return QuestionItemDTO.builder()
                    .id(question.getId())
                    .title(question.getTitle())
                    .sectionType(question.getSectionType().name())
                    .sectionName(question.getSectionName())
                    .paperId(question.getPaperId())
                    .paperName(paper != null ? paper.getName() : "")
                    .paperCategory(paper != null ? paper.getCategory() : "")
                    .status(status)
                    .isFavorited(favoritedQuestionIds.contains(question.getId()))
                    .createdAt(question.getCreatedAt())
                    .build();
            })
            .collect(Collectors.toList());
        
        // 状态筛选
        items = filterByStatus(items, request.getStatus(), completedQuestionIds, favoritedQuestionIds);
        
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
        Double accuracyDouble = userAnswerDetailRepository.calculateTodayAccuracy(userId);
        
        Integer accuracy = 0;
        if (accuracyDouble != null) {
            accuracy = (int) Math.round(accuracyDouble * 100);
        }
        
        return TodayStatsDTO.builder()
            .count(count != null ? count : 0L)
            .accuracy(accuracy)
            .build();
    }
    
    /**
     * 添加收藏
     */
    @Transactional
    public void addFavorite(Long userId, FavoriteRequest request) {
        Long questionId;
        
        if ("paper".equals(request.getType())) {
            // 试卷收藏：收藏该试卷的所有题目
            List<QuestionBank> questions = questionBankRepository.findByPaperIdOrderBySortOrder(request.getId());
            for (QuestionBank question : questions) {
                if (!userFavoriteRepository.existsByUserIdAndQuestionId(userId, question.getId())) {
                    UserFavorite favorite = new UserFavorite();
                    favorite.setUserId(userId);
                    favorite.setQuestionId(question.getId());
                    userFavoriteRepository.save(favorite);
                }
            }
        } else if ("question".equals(request.getType())) {
            questionId = request.getId();
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
            // 取消试卷收藏：取消该试卷的所有题目收藏
            List<QuestionBank> questions = questionBankRepository.findByPaperIdOrderBySortOrder(request.getId());
            for (QuestionBank question : questions) {
                userFavoriteRepository.deleteByUserIdAndQuestionId(userId, question.getId());
            }
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
        
        // 批量处理答案
        List<SubmitAnswerResponse.AnswerDetail> details = new ArrayList<>();
        BigDecimal totalScore = BigDecimal.ZERO;
        int correctCount = 0;
        
        for (SubmitAnswerRequest.AnswerItem answerItem : request.getAnswers()) {
            QuestionSubItem subItem = subItemMap.get(answerItem.getSubItemId());
            if (subItem == null) continue;
            
            // 解析正确答案
            List<String> correctAnswer = parseAnswer(subItem.getAnswer());
            
            // 校验答案
            boolean isCorrect = checkAnswer(answerItem.getAnswer(), correctAnswer, subItem.getItemType());
            BigDecimal scoreObtained = isCorrect ? subItem.getScoreValue() : BigDecimal.ZERO;
            
            if (isCorrect) {
                correctCount++;
            }
            totalScore = totalScore.add(scoreObtained);
            
            // 保存答题详情
            UserAnswerDetail detail = new UserAnswerDetail();
            detail.setRecordId(record.getId());
            detail.setSubItemId(subItem.getId());
            detail.setUserId(userId);
            detail.setUserContent(convertAnswerToJson(answerItem.getAnswer()));
            detail.setIsCorrect(isCorrect ? 1 : 0);
            detail.setScoreObtained(scoreObtained);
            userAnswerDetailRepository.save(detail);
            
            // 构建详情 DTO
            details.add(SubmitAnswerResponse.AnswerDetail.builder()
                .subItemId(subItem.getId())
                .userAnswer(answerItem.getAnswer())
                .correctAnswer(correctAnswer)
                .isCorrect(isCorrect)
                .scoreObtained(scoreObtained)
                .explanation(subItem.getExplanation())
                .build());
        }
        
        // 更新考试记录
        record.setStatus(UserExamRecord.Status.completed);
        record.setTotalScore(totalScore);
        record.setCompletedAt(LocalDateTime.now());
        userExamRecordRepository.save(record);
        
        // 计算正确率
        int accuracy = request.getAnswers().isEmpty() ? 0 : 
            (int) Math.round((double) correctCount / request.getAnswers().size() * 100);
        
        return SubmitAnswerResponse.builder()
            .recordId(record.getId())
            .score(totalScore)
            .accuracy(accuracy)
            .totalQuestions(request.getAnswers().size())
            .correctCount(correctCount)
            .details(details)
            .build();
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
