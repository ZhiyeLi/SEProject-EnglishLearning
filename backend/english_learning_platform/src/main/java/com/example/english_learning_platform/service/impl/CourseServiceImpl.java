package com.example.english_learning_platform.service.impl;

import com.example.english_learning_platform.dto.CourseDTO;
import com.example.english_learning_platform.dto.CourseDetailDTO;
import com.example.english_learning_platform.entity.Course;
import com.example.english_learning_platform.entity.UserCourseFavorite;
import com.example.english_learning_platform.entity.UserCourseProgress;
import com.example.english_learning_platform.repository.CourseRepository;
import com.example.english_learning_platform.repository.UserCourseFavoriteRepository;
import com.example.english_learning_platform.repository.UserCourseProgressRepository;
import com.example.english_learning_platform.service.CourseService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final UserCourseProgressRepository progressRepository;
    private final UserCourseFavoriteRepository favoriteRepository;

    private static final Map<String, String> coverCache = new ConcurrentHashMap<>();
    private static final RestTemplate restTemplate = new RestTemplate();
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Pattern BV_PATTERN = Pattern.compile("BV[a-zA-Z0-9]+");

    // 已知课程封面映射（已验证），确保即使 API 不通也能正常显示
    static {
        coverCache.put("BV1Et421u7nq", "http://i1.hdslb.com/bfs/archive/fcf3d0fc699ae6967f843ddbbf553dea28b06e64.jpg");
        coverCache.put("BV17T411u7jj", "http://i2.hdslb.com/bfs/archive/3d00f1c61b8224b6533c1877f822eec5112e28ff.jpg");
        coverCache.put("BV1oD4y1N7uH", "http://i1.hdslb.com/bfs/archive/c5ec2200369303a949052831c9e6a37411cd19ee.png");
        coverCache.put("BV1UXq5YWEoT", "http://i2.hdslb.com/bfs/archive/98e9202e1b874279a649bb5ff00804f917893662.jpg");
        coverCache.put("BV1yi4y1P7Ng", "http://i1.hdslb.com/bfs/archive/0e27d7e49317a6d2b8a9e59ffdccfcfc6b9666af.png");
        coverCache.put("BV1Fg411w7Bt", "http://i2.hdslb.com/bfs/archive/8bdec2b58006d476fd41a99f3c00d45c6e4d114d.png");
    }

    public CourseServiceImpl(CourseRepository courseRepository,
                             UserCourseProgressRepository progressRepository,
                             UserCourseFavoriteRepository favoriteRepository) {
        this.courseRepository = courseRepository;
        this.progressRepository = progressRepository;
        this.favoriteRepository = favoriteRepository;
    }

    /**
     * 解析课程封面图 URL（公开方法，供 Controller 代理使用）。
     * 如果已有 HTTP 封面则直接用；否则从 B 站 API 获取（带缓存）。
     */
    public String resolveCoverUrl(String coverImage, String videoUrl) {
        // 已有有效 HTTP URL 则直接用
        if (coverImage != null && (coverImage.startsWith("http://") || coverImage.startsWith("https://"))) {
            return coverImage;
        }

        // 从视频链接提取 BV 号
        if (videoUrl == null || videoUrl.isEmpty()) return coverImage;
        Matcher m = BV_PATTERN.matcher(videoUrl);
        if (!m.find()) return coverImage;
        String bvid = m.group();

        return resolveBilibiliCover(bvid, coverImage);
    }

    /**
     * 通过 BV 号获取 B 站封面（带缓存）。
     * 返回封面 URL，失败时返回 fallback。
     */
    public String resolveBilibiliCover(String bvid, String fallback) {
        if (coverCache.containsKey(bvid)) {
            String cached = coverCache.get(bvid);
            return (cached != null && !cached.isEmpty()) ? cached : fallback;
        }

        try {
            String apiUrl = "https://api.bilibili.com/x/web-interface/view?bvid=" + bvid;
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
            headers.set("Referer", "https://www.bilibili.com/");
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<String> resp = restTemplate.exchange(apiUrl, HttpMethod.GET, entity, String.class);

            if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
                JsonNode root = objectMapper.readTree(resp.getBody());
                String pic = root.path("data").path("pic").asText(null);
                if (pic != null && !pic.isEmpty()) {
                    coverCache.put(bvid, pic);
                    return pic;
                }
            }
        } catch (Exception e) {
            // 静默失败，返回 fallback
        }

        coverCache.put(bvid, "");
        return fallback;
    }

    @Override
    public Page<CourseDTO> getCourses(String keyword, String tag, int page, int size, Long userId) {
        List<Course> all = courseRepository.findAll();

        // 筛选
        List<Course> filtered = all.stream()
            .filter(c -> tag == null || tag.isEmpty() || tag.equals("all") || tag.equals(c.getTag()))
            .filter(c -> {
                if (keyword == null || keyword.isEmpty()) return true;
                String kw = keyword.toLowerCase();
                return (c.getName() != null && c.getName().toLowerCase().contains(kw))
                    || (c.getDescription() != null && c.getDescription().toLowerCase().contains(kw));
            })
            .collect(Collectors.toList());

        // 获取用户进度和收藏状态
        List<UserCourseProgress> progressList = progressRepository.findByUserId(userId);
        Map<Long, String> progressMap = progressList.stream()
            .collect(Collectors.toMap(UserCourseProgress::getCourseId, UserCourseProgress::getStatus));

        List<UserCourseFavorite> favorites = favoriteRepository.findByUserId(userId);
        Set<Long> favoriteIds = favorites.stream()
            .map(UserCourseFavorite::getCourseId)
            .collect(Collectors.toSet());

        // 转换为 DTO
        List<CourseDTO> dtos = filtered.stream()
            .map(c -> new CourseDTO(
                c.getId(), c.getName(), c.getDescription(), c.getTag(),
                resolveCoverUrl(c.getCoverImage(), c.getVideoUrl()),
                c.getVideoUrl(), c.getLevel(),
                progressMap.getOrDefault(c.getId(), null),
                favoriteIds.contains(c.getId())
            ))
            .collect(Collectors.toList());

        // 分页
        int start = Math.min(page * size, dtos.size());
        int end = Math.min(start + size, dtos.size());
        List<CourseDTO> pageContent = dtos.subList(start, end);

        return new PageImpl<>(pageContent, PageRequest.of(page, size), dtos.size());
    }

    @Override
    public CourseDetailDTO getCourseById(Long courseId, Long userId) {
        Course c = courseRepository.findById(courseId)
            .orElseThrow(() -> new RuntimeException("课程不存在"));

        String status = progressRepository.findByUserIdAndCourseId(userId, courseId)
            .map(UserCourseProgress::getStatus).orElse(null);

        boolean fav = favoriteRepository.findByUserIdAndCourseId(userId, courseId).isPresent();

        return new CourseDetailDTO(
            c.getId(), c.getName(), c.getDescription(), c.getTag(),
            resolveCoverUrl(c.getCoverImage(), c.getVideoUrl()),
            c.getVideoUrl(), c.getLevel(), status, fav
        );
    }

    @Override
    @Transactional
    public void updateProgress(Long userId, Long courseId, String status) {
        UserCourseProgress progress = progressRepository
            .findByUserIdAndCourseId(userId, courseId)
            .orElseGet(() -> {
                UserCourseProgress p = new UserCourseProgress();
                p.setUserId(userId);
                p.setCourseId(courseId);
                p.setStatus("learning");
                return p;
            });

        progress.setStatus(status);
        progress.setLastAccessedAt(LocalDateTime.now());

        if ("completed".equals(status)) {
            progress.setCompletedAt(LocalDateTime.now());
        }

        progressRepository.save(progress);
    }

    @Override
    @Transactional
    public void markComplete(Long userId, Long courseId) {
        updateProgress(userId, courseId, "completed");
    }

    @Override
    @Transactional
    public boolean toggleFavorite(Long userId, Long courseId) {
        Optional<UserCourseFavorite> existing = favoriteRepository
            .findByUserIdAndCourseId(userId, courseId);

        if (existing.isPresent()) {
            favoriteRepository.delete(existing.get());
            return false;
        } else {
            UserCourseFavorite fav = new UserCourseFavorite();
            fav.setUserId(userId);
            fav.setCourseId(courseId);
            favoriteRepository.save(fav);
            return true;
        }
    }

    @Override
    public List<Long> getFavoriteIds(Long userId) {
        return favoriteRepository.findByUserId(userId).stream()
            .map(UserCourseFavorite::getCourseId)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void seedCourseData() {
        // 修复已有课程的无效封面（非 HTTP URL 的视为无效）
        List<Course> all = courseRepository.findAll();
        for (Course c : all) {
            if (c.getCoverImage() == null
                || (!c.getCoverImage().startsWith("http://") && !c.getCoverImage().startsWith("https://"))) {
                String bvid = extractBvid(c.getVideoUrl());
                if (bvid != null) {
                    String resolved = resolveBilibiliCover(bvid, "");
                    if (!resolved.isEmpty()) {
                        c.setCoverImage(resolved);
                        courseRepository.save(c);
                    }
                }
            }
        }

        // 增量 seed：封面 URL 已全部通过 B 站 API 验证
        add("零基础系统学英语",
            "从零开始，外教名师教你系统的学习英语。涵盖发音、词汇、句型基础，适合完全没有英语基础的学习者。",
            "none", "https://www.bilibili.com/video/BV1Et421u7nq", "beginner",
            "http://i1.hdslb.com/bfs/archive/fcf3d0fc699ae6967f843ddbbf553dea28b06e64.jpg");
        add("中学英语优质公开课",
            "全国初中英语优质公开课 | 黄佳妍 | 八年级 | 阅读课 | 专家点评：程晓堂 张雪莲",
            "middle", "https://www.bilibili.com/video/BV17T411u7jj", "intermediate",
            "http://i2.hdslb.com/bfs/archive/3d00f1c61b8224b6533c1877f822eec5112e28ff.jpg");
        add("大学英语四六级考试全套精讲课程",
            "用最通俗易懂的方式带你走进英语的世界，十天带你打好基础，逐渐走上英语学霸之路。",
            "college", "https://www.bilibili.com/video/BV1oD4y1N7uH", "advanced",
            "http://i1.hdslb.com/bfs/archive/c5ec2200369303a949052831c9e6a37411cd19ee.png");
        add("幼儿英语启蒙动画",
            "清华幼儿英语语感启蒙，清华附小英语动画启蒙+1-4年级英语课程，适合零基础宝宝的慢速磨耳朵英语动画片。",
            "primary", "https://www.bilibili.com/video/BV1UXq5YWEoT", "beginner",
            "http://i2.hdslb.com/bfs/archive/98e9202e1b874279a649bb5ff00804f917893662.jpg");
        add("小学生英语对话",
            "通过人物对话，深度学习英语。",
            "primary", "https://www.bilibili.com/video/BV1yi4y1P7Ng", "beginner",
            "http://i1.hdslb.com/bfs/archive/0e27d7e49317a6d2b8a9e59ffdccfcfc6b9666af.png");
        add("大学四级词汇",
            "从基础写作规范到高级表达技巧，全面提升大学英语写作能力，适合备考四六级及日常学术写作。",
            "college", "https://www.bilibili.com/video/BV1Fg411w7Bt", "advanced",
            "http://i2.hdslb.com/bfs/archive/8bdec2b58006d476fd41a99f3c00d45c6e4d114d.png");

        // === 新增课程（封面 URL 已通过 B 站 API 验证） ===
        add("英语零基础入门必备 2025全新合集",
            "音标+语法+口语+听力+四级，一站式从零开始，零基础自学必备全套教程。",
            "none", "https://www.bilibili.com/video/BV1TXjizrEo3", "beginner",
            "http://i1.hdslb.com/bfs/archive/f170446d131dac342814d589145e53c1122f4bde.jpg");
        add("从零基础到无障碍交流全课程",
            "通过听英文歌、看电影、看动画片等趣味方式，覆盖字母、音标、拼读到对话全流程。",
            "none", "https://www.bilibili.com/video/BV1x9pDzZExi", "beginner",
            "http://i0.hdslb.com/bfs/archive/67bb86b1b18644df6402f5a5b051bed33d8b3ded.jpg");
        add("全122集 零基础完美口语速成",
            "2024最新版自学英语，地道口语+国际音标+自然拼读+语句规则，系统提升口语能力。",
            "none", "https://www.bilibili.com/video/BV1cWxQezESM", "beginner",
            "http://i1.hdslb.com/bfs/archive/01b23a2c6a606e0d27079580e2c440ab206bfc3b.png");
        add("48个音标快速记忆法",
            "英语必修课，超级干货，快速掌握48个国际音标的标准发音和记忆技巧。",
            "primary", "https://www.bilibili.com/video/BV1RtuEzEEpB", "beginner",
            "http://i2.hdslb.com/bfs/archive/1a2954df71c3c709d47b8e9807e3d6a0af45b211.jpg");
        add("BBC官方音标教程 92集全",
            "BBC官方英式英语发音教程，零基础学音标，中英双语字幕，英音英伦腔口语养成。",
            "primary", "https://www.bilibili.com/video/BV1b4b1zYEFW", "beginner",
            "http://i2.hdslb.com/bfs/archive/8f020944d8b98ce46df080fc918081e040e29766.jpg");
        add("英语语法精讲合集（英语兔）",
            "全面、通俗、有趣，从零打造系统语法体系，B站播放量最高的英语语法教程之一。",
            "middle", "https://www.bilibili.com/video/BV1XY411J7aG", "intermediate",
            "http://i2.hdslb.com/bfs/archive/2f68c61bb7c4a05dc707e41c1c97560594a5a53a.jpg");
        add("英语语法全程课 合集1-119（全B站最强）",
            "零基础起点，全面覆盖词法句法时态语态，初中高中四六级英语小白的救星。",
            "middle", "https://www.bilibili.com/video/BV1D7411J71b", "intermediate",
            "http://i0.hdslb.com/bfs/archive/9ce8f8a6f5ac6914e6fdc58e26ba031ef19eb091.jpg");
        add("刘晓艳 英语语法零基础急速通关",
            "四六级+考研适用，20+小时系统语法讲解，快速搭建语法框架。",
            "college", "https://www.bilibili.com/video/BV1eC1WBgE2G", "intermediate",
            "http://i2.hdslb.com/bfs/archive/87833d93dadd6603c162e810238cc5dc9eeb8747.jpg");
        add("刘晓艳 语法长难句精讲",
            "配套《不就是语法和长难句吗》，深入攻克四六级和考研英语的长难句分析。",
            "college", "https://www.bilibili.com/video/BV1KcXfBGEWM", "advanced",
            "http://i0.hdslb.com/bfs/archive/81a15d76440fb2a9de38be47eee1c5f972e31fcb.jpg");
        add("78集全 零基础轻松跟老外对话",
            "从简单英语开始学起，真正零基础入门，1个月学完轻松跟老外对话，实用口语导向。",
            "none", "https://www.bilibili.com/video/BV1NPtizeEfb", "beginner",
            "http://i2.hdslb.com/bfs/archive/39d3bdcd9f2731c6ac07ae2ac799830bed577ad9.jpg");
        add("英语口语 商务旅行留学",
            "实用场景口语训练，覆盖商务谈判、旅行对话、留学生活等高频场景。",
            "college", "https://www.bilibili.com/video/BV1u7411M7yA", "advanced",
            "http://i1.hdslb.com/bfs/archive/d8cc18e53b993b507044c01cee51376d9257ea91.jpg");
        add("TED官方 最强英语口语老师",
            "来自油管最强英语口语老师，0到9分口语系统练习，逐级提升表达力。",
            "college", "https://www.bilibili.com/video/BV1Y4421Z7rt", "advanced",
            "http://i2.hdslb.com/bfs/archive/23e274ec4aabdf43d5d1ad4c9e137b05d9de7b33.png");
        add("场景英语对话 生动有趣",
            "通过生活化场景对话学英语，视频有趣生动，告别枯燥，轻松掌握日常表达。",
            "primary", "https://www.bilibili.com/video/BV1emBiYcEAV", "beginner",
            "http://i0.hdslb.com/bfs/archive/45359bc1c4fe12ab668b252646a9ffe2669503e3.jpg");
    }

    private String extractBvid(String videoUrl) {
        if (videoUrl == null) return null;
        Matcher m = BV_PATTERN.matcher(videoUrl);
        return m.find() ? m.group() : null;
    }

    private void add(String name, String desc, String tag, String videoUrl, String level, String coverImage) {
        List<Course> existing = courseRepository.findAll().stream()
            .filter(c -> videoUrl.equals(c.getVideoUrl()))
            .collect(Collectors.toList());
        if (!existing.isEmpty()) return;

        Course c = new Course();
        c.setName(name);
        c.setDescription(desc);
        c.setTag(tag);
        c.setVideoUrl(videoUrl);
        c.setLevel(level);
        c.setCoverImage(coverImage);
        courseRepository.save(c);
    }
}
