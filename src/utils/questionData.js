/**
 * 题库模拟数据
 * 包含题目类型、难度等级、题目数据等
 */

import { reactive } from "vue";

// 题目类型枚举
export const QUESTION_TYPES = {
  LISTENING: { id: "listening", name: "听力", icon: "fa-headphones" },
  READING: { id: "reading", name: "阅读", icon: "fa-book-open" },
  WRITING: { id: "writing", name: "写作", icon: "fa-pen" },
  GRAMMAR: { id: "grammar", name: "语法", icon: "fa-spell-check" },
  VOCABULARY: { id: "vocabulary", name: "词汇", icon: "fa-font" },
};

// 难度等级枚举
export const DIFFICULTY_LEVELS = {
  BEGINNER: { id: "beginner", name: "初级", stars: 1, color: "green" },
  CET4_6: { id: "cet4-6", name: "四六级", stars: 2, color: "blue" },
  POSTGRADUATE: { id: "postgraduate", name: "考研", stars: 3, color: "purple" },
  TOEFL_IELTS: {
    id: "toefl-ielts",
    name: "托福雅思",
    stars: 4,
    color: "orange",
  },
  PROFESSIONAL: { id: "professional", name: "专业", stars: 5, color: "red" },
};

// 模拟课程数据
export const COURSES = [
  {
    id: "course1",
    name: "高级英语写作",
    chapters: ["Unit 1", "Unit 2", "Unit 3", "Unit 4"],
  },
  {
    id: "course2",
    name: "商务英语",
    chapters: ["Chapter 1", "Chapter 2", "Chapter 3"],
  },
  {
    id: "course3",
    name: "英语听说训练",
    chapters: ["Lesson 1", "Lesson 2", "Lesson 3", "Lesson 4", "Lesson 5"],
  },
  {
    id: "course4",
    name: "英语语法精讲",
    chapters: ["基础语法", "进阶语法", "高级语法"],
  },
];

// 模拟题目数据 - 使用 reactive 使其具有响应性
export const QUESTIONS = reactive([
  {
    id: "q001",
    type: "reading",
    difficulty: "cet4-6",
    title: "The Impact of Social Media on Modern Communication",
    preview:
      "Social media has fundamentally changed the way people communicate in the 21st century...",
    content: `Social media has fundamentally changed the way people communicate in the 21st century. Platforms like Facebook, Twitter, and Instagram have become integral parts of daily life for billions of people worldwide.

The rise of social media has brought both benefits and challenges. On one hand, it has made it easier than ever to stay connected with friends and family, regardless of geographical distance. On the other hand, concerns about privacy, mental health, and the spread of misinformation have become increasingly prominent.

Studies have shown that excessive social media use can lead to feelings of anxiety and depression, particularly among young people. The constant comparison with others' carefully curated online personas can negatively impact self-esteem and body image.

However, social media also serves as a powerful tool for social movements and awareness campaigns. The #MeToo movement and various climate change initiatives have gained significant traction through these platforms, demonstrating their potential for positive social change.`,
    questions: [
      {
        id: "q001-1",
        question:
          "According to the passage, what is one benefit of social media?",
        options: [
          "A. It improves physical health",
          "B. It makes staying connected easier",
          "C. It eliminates privacy concerns",
          "D. It reduces misinformation",
        ],
        answer: "B",
        explanation:
          '文章第二段提到 "it has made it easier than ever to stay connected with friends and family"，说明社交媒体让保持联系变得更容易。',
      },
      {
        id: "q001-2",
        question:
          "What negative effect of social media is mentioned in the passage?",
        options: [
          "A. Increased travel costs",
          "B. Reduced literacy rates",
          "C. Feelings of anxiety and depression",
          "D. Decreased internet usage",
        ],
        answer: "C",
        explanation:
          '文章第三段明确指出 "excessive social media use can lead to feelings of anxiety and depression"，过度使用社交媒体会导致焦虑和抑郁。',
      },
      {
        id: "q001-3",
        question: 'The word "curated" in paragraph 3 is closest in meaning to:',
        options: [
          "A. Random",
          "B. Carefully selected",
          "C. Authentic",
          "D. Unfiltered",
        ],
        answer: "B",
        explanation:
          '"Curated" 意为精心挑选的、策划的，在这里指人们在社交媒体上展示的是经过精心挑选的内容。',
      },
    ],
    relatedCourse: "course1",
    relatedChapter: "Unit 3",
    highlightWords: ["social media", "communication", "anxiety"],
    tags: ["社交媒体", "现代通讯", "心理健康"],
    createdAt: "2024-01-15",
    status: "not-done",
  },
  {
    id: "q002",
    type: "listening",
    difficulty: "cet4-6",
    title: "Campus Life - Student Interview",
    preview:
      "A conversation between a student and an interviewer about university life...",
    content: `[Audio Script]
Interviewer: Good morning! Today we're talking with Sarah, a third-year student at State University. Sarah, how has your college experience been so far?

Sarah: Good morning! It's been quite a journey. The first year was definitely the most challenging as I had to adjust to living away from home and managing my own schedule.

Interviewer: What strategies did you use to adapt?

Sarah: I joined several student clubs which helped me make friends quickly. I also established a strict study routine - I always studied at the library from 7 to 10 PM every weeknight.

Interviewer: That sounds very disciplined! What advice would you give to incoming freshmen?

Sarah: Don't be afraid to ask for help. Professors have office hours for a reason, and there are tutoring services available. Also, take care of your mental health - it's okay to take breaks when you need them.`,
    questions: [
      {
        id: "q002-1",
        question: "What year is Sarah currently in at university?",
        options: [
          "A. First year",
          "B. Second year",
          "C. Third year",
          "D. Fourth year",
        ],
        answer: "C",
        explanation:
          '对话开头提到 "a third-year student at State University"，Sarah是大三学生。',
      },
      {
        id: "q002-2",
        question: "How did Sarah make friends at university?",
        options: [
          "A. By attending parties",
          "B. By joining student clubs",
          "C. By living in a dorm",
          "D. By playing sports",
        ],
        answer: "B",
        explanation:
          'Sarah说 "I joined several student clubs which helped me make friends quickly"。',
      },
    ],
    relatedCourse: "course3",
    relatedChapter: "Lesson 2",
    audioUrl: "/audio/campus-life.mp3",
    highlightWords: ["campus", "student", "freshman"],
    tags: ["校园生活", "学生访谈", "适应大学"],
    createdAt: "2024-01-20",
    status: "not-done",
  },
  {
    id: "q003",
    type: "grammar",
    difficulty: "beginner",
    title: "虚拟语气专项练习",
    preview:
      "练习英语中的虚拟语气用法，包括与现在、过去、将来事实相反的假设...",
    content: `虚拟语气（Subjunctive Mood）用于表示假设、愿望、建议等非真实情况。

1. 与现在事实相反：
   If + 主语 + 过去式, 主语 + would/could/might + 动词原形
   例：If I were you, I would accept the offer.

2. 与过去事实相反：
   If + 主语 + had + 过去分词, 主语 + would/could/might + have + 过去分词
   例：If I had studied harder, I would have passed the exam.

3. 与将来事实相反：
   If + 主语 + should/were to + 动词原形, 主语 + would/could/might + 动词原形
   例：If the sun were to rise in the west, I would believe you.`,
    questions: [
      {
        id: "q003-1",
        question: "If I ___ rich, I would travel around the world.",
        options: ["A. am", "B. was", "C. were", "D. be"],
        answer: "C",
        explanation:
          "与现在事实相反的虚拟语气，be动词一律用were，不论主语是单数还是复数。",
      },
      {
        id: "q003-2",
        question: "She wishes she ___ the meeting yesterday.",
        options: [
          "A. attended",
          "B. had attended",
          "C. would attend",
          "D. attends",
        ],
        answer: "B",
        explanation:
          "wish后接与过去事实相反的情况时，用过去完成时had + 过去分词。",
      },
      {
        id: "q003-3",
        question: "It is essential that every student ___ on time.",
        options: ["A. arrives", "B. arrive", "C. arrived", "D. has arrived"],
        answer: "B",
        explanation:
          "在essential, important, necessary等词后的that从句中，谓语动词用原形（虚拟语气）。",
      },
    ],
    relatedCourse: "course4",
    relatedChapter: "进阶语法",
    highlightWords: ["subjunctive", "were", "wish"],
    tags: ["虚拟语气", "语法", "从句"],
    createdAt: "2024-01-25",
    status: "done",
    lastResult: "correct",
    lastAttemptDate: "2024-01-28",
  },
  {
    id: "q004",
    type: "vocabulary",
    difficulty: "postgraduate",
    title: "考研核心词汇 - 高频词组",
    preview: "掌握考研英语中的高频词汇和词组搭配...",
    content: `考研英语核心词汇精选：

1. virtual reality (虚拟现实) - 名词短语
   The development of virtual reality technology has opened new possibilities in education.

2. sustainable development (可持续发展) - 名词短语
   Sustainable development requires balancing economic growth with environmental protection.

3. unprecedented (前所未有的) - 形容词
   The pandemic caused unprecedented disruption to global supply chains.

4. deteriorate (恶化) - 动词
   Air quality continues to deteriorate in major cities.

5. paradigm shift (范式转变) - 名词短语
   The internet caused a paradigm shift in how information is shared.`,
    questions: [
      {
        id: "q004-1",
        question:
          "The company aims to achieve ___ development by reducing its carbon footprint.",
        options: [
          "A. sustainable",
          "B. virtual",
          "C. unprecedented",
          "D. deteriorating",
        ],
        answer: "A",
        explanation:
          'sustainable development 意为"可持续发展"，是环保和经济发展领域的常用术语。',
      },
      {
        id: "q004-2",
        question:
          "The situation began to ___ after the government failed to intervene.",
        options: [
          "A. improve",
          "B. deteriorate",
          "C. stabilize",
          "D. fluctuate",
        ],
        answer: "B",
        explanation:
          'deteriorate 意为"恶化"，与failed to intervene（未能干预）的语境相符。',
      },
    ],
    relatedCourse: null,
    relatedChapter: null,
    highlightWords: ["virtual reality", "sustainable", "paradigm"],
    tags: ["考研词汇", "高频词组", "词汇辨析"],
    createdAt: "2024-02-01",
    status: "done",
    lastResult: "wrong",
    lastAttemptDate: "2024-02-05",
  },
  {
    id: "q005",
    type: "writing",
    difficulty: "toefl-ielts",
    title: "IELTS Writing Task 2 - Technology and Society",
    preview:
      "Some people believe that technology has made our lives more complicated. To what extent do you agree or disagree?",
    content: `Writing Task 2 (250 words minimum)

Topic: Some people believe that technology has made our lives more complicated. To what extent do you agree or disagree?

Sample Essay Structure:

Introduction:
- Paraphrase the question
- State your position
- Outline your main points

Body Paragraph 1 - Arguments for technology making life complicated:
- Information overload
- Constant connectivity and work-life balance issues
- Cybersecurity concerns

Body Paragraph 2 - Arguments against / How technology simplifies life:
- Automation of routine tasks
- Improved communication and access to information
- Healthcare and safety improvements

Conclusion:
- Summarize your position
- Provide a balanced final thought`,
    questions: [
      {
        id: "q005-1",
        question: "请写一篇不少于250词的议论文，讨论科技是否使生活变得更复杂。",
        type: "essay",
        sampleAnswer: `Technology has undeniably transformed our daily lives, and while some argue it has made things more complicated, I believe the benefits largely outweigh the drawbacks.

Admittedly, technology has introduced certain complexities. The constant stream of notifications and the expectation of immediate responses can create stress and blur the boundaries between work and personal life. Moreover, concerns about data privacy and cybersecurity have added new worries to our daily existence.

However, technology has simplified numerous aspects of life. Online banking, shopping, and communication have eliminated the need for time-consuming trips and paperwork. Medical advances have improved diagnosis and treatment, while GPS navigation has made travel more accessible. Furthermore, remote work technology has provided flexibility that was previously unimaginable.

In conclusion, while technology does present some challenges, its overall impact has been to simplify and enhance our lives. The key lies in using technology mindfully and maintaining healthy boundaries.`,
        explanation:
          "这篇范文展示了如何组织一篇平衡的议论文：首先承认对方观点的合理性，然后阐述自己的主要论点，最后得出结论。",
      },
    ],
    relatedCourse: "course1",
    relatedChapter: "Unit 4",
    highlightWords: ["technology", "complicated", "agree or disagree"],
    tags: ["雅思写作", "Task 2", "科技话题"],
    createdAt: "2024-02-10",
    status: "not-done",
  },
  {
    id: "q006",
    type: "reading",
    difficulty: "professional",
    title: "Academic Paper: Climate Change Impact on Marine Ecosystems",
    preview:
      "This study examines the multifaceted impacts of climate change on marine biodiversity...",
    content: `Abstract: This study examines the multifaceted impacts of climate change on marine biodiversity, with particular focus on coral reef ecosystems and their associated species.

Introduction:
Marine ecosystems are experiencing unprecedented changes due to anthropogenic climate change. Rising sea temperatures, ocean acidification, and altered precipitation patterns are fundamentally altering the physical and chemical properties of ocean environments.

Coral reefs, often termed the "rainforests of the sea," are particularly vulnerable to these changes. When water temperatures rise above certain thresholds, corals expel their symbiotic algae (zooxanthellae), resulting in coral bleaching. Extended periods of elevated temperatures can lead to coral mortality and the collapse of reef ecosystems.

Methodology:
This research synthesizes data from 127 peer-reviewed studies conducted between 2010 and 2023. We employed meta-analysis techniques to quantify the relationship between temperature anomalies and coral bleaching events across different geographical regions.

Results:
Our analysis reveals a statistically significant correlation (p < 0.001) between sea surface temperature increases of 1°C above historical averages and mass bleaching events. Furthermore, the recovery time for bleached corals has increased from an average of 15 years in the 1990s to 25 years in recent events.`,
    questions: [
      {
        id: "q006-1",
        question: "What happens when corals experience bleaching?",
        options: [
          "A. They absorb more sunlight",
          "B. They expel their symbiotic algae",
          "C. They grow faster",
          "D. They produce more offspring",
        ],
        answer: "B",
        explanation:
          '文章明确说明 "corals expel their symbiotic algae (zooxanthellae), resulting in coral bleaching"。',
      },
      {
        id: "q006-2",
        question:
          "According to the study, how has coral recovery time changed?",
        options: [
          "A. Decreased from 25 to 15 years",
          "B. Remained constant at 15 years",
          "C. Increased from 15 to 25 years",
          "D. Decreased significantly",
        ],
        answer: "C",
        explanation:
          '文章结果部分指出 "the recovery time for bleached corals has increased from an average of 15 years in the 1990s to 25 years in recent events"。',
      },
      {
        id: "q006-3",
        question: 'The term "anthropogenic" most likely means:',
        options: [
          "A. Natural",
          "B. Caused by humans",
          "C. Related to animals",
          "D. Concerning geology",
        ],
        answer: "B",
        explanation:
          '"Anthropogenic" 来自希腊语 anthropos（人类）和 genic（产生的），意为"人为的、人类活动导致的"。',
      },
    ],
    relatedCourse: null,
    relatedChapter: null,
    highlightWords: ["climate change", "coral reef", "biodiversity"],
    tags: ["学术论文", "气候变化", "海洋生态"],
    createdAt: "2024-02-15",
    status: "not-done",
  },
  {
    id: "q007",
    type: "listening",
    difficulty: "toefl-ielts",
    title: "IELTS Listening - Museum Tour Guide",
    preview:
      "A museum guide explaining the history and significance of ancient artifacts...",
    content: `[Audio Script]
Guide: Welcome to the National History Museum. Today, I'll be taking you through our Egyptian collection, which houses over 3,000 artifacts dating back to 3000 BCE.

Our first exhibit is the Rosetta Stone replica. The original stone, now in the British Museum, was crucial in deciphering Egyptian hieroglyphics. It contains the same text written in three different scripts: hieroglyphic, demotic, and ancient Greek.

Moving on, you'll notice our collection of canopic jars. These vessels were used during mummification to store the internal organs of the deceased. Each jar was protected by one of the Four Sons of Horus.

Now, if you look to your right, you'll see our prize possession - a perfectly preserved papyrus scroll containing excerpts from the Book of the Dead. This scroll would have been placed in the tomb to guide the deceased through the afterlife.

Please feel free to ask questions as we continue the tour.`,
    questions: [
      {
        id: "q007-1",
        question: "What was the Rosetta Stone important for?",
        options: [
          "A. Building pyramids",
          "B. Deciphering hieroglyphics",
          "C. Storing artifacts",
          "D. Mummification process",
        ],
        answer: "B",
        explanation:
          '导游说 "The original stone...was crucial in deciphering Egyptian hieroglyphics"。',
      },
      {
        id: "q007-2",
        question: "What were canopic jars used for?",
        options: [
          "A. Storing water",
          "B. Decorating tombs",
          "C. Storing internal organs",
          "D. Holding jewelry",
        ],
        answer: "C",
        explanation:
          '导游解释 "These vessels were used during mummification to store the internal organs of the deceased"。',
      },
    ],
    relatedCourse: "course3",
    relatedChapter: "Lesson 4",
    audioUrl: "/audio/museum-tour.mp3",
    highlightWords: ["museum", "artifact", "hieroglyphics"],
    tags: ["雅思听力", "博物馆", "历史文化"],
    createdAt: "2024-02-20",
    status: "done",
    lastResult: "correct",
    lastAttemptDate: "2024-02-22",
  },
  {
    id: "q008",
    type: "grammar",
    difficulty: "cet4-6",
    title: "定语从句综合练习",
    preview: "掌握定语从句的各种用法，包括限制性和非限制性定语从句...",
    content: `定语从句（Attributive Clause）用于修饰名词或代词，使句子更加具体和生动。

1. 关系代词的选择：
   - who/whom：指人
   - which：指物
   - that：指人或物
   - whose：表示所属关系

2. 关系副词的选择：
   - when：时间
   - where：地点
   - why：原因

3. 限制性 vs 非限制性定语从句：
   - 限制性：对先行词起限定作用，不可省略
   - 非限制性：对先行词起补充说明作用，用逗号隔开

例句：
- The book which/that I bought yesterday is very interesting. (限制性)
- My sister, who lives in London, is a doctor. (非限制性)`,
    questions: [
      {
        id: "q008-1",
        question: "The girl ___ father is a doctor studies very hard.",
        options: ["A. who", "B. whom", "C. whose", "D. which"],
        answer: "C",
        explanation: '这里需要表示所属关系"女孩的父亲"，用whose引导定语从句。',
      },
      {
        id: "q008-2",
        question: "This is the place ___ we first met.",
        options: ["A. which", "B. where", "C. that", "D. when"],
        answer: "B",
        explanation: "先行词是place（地点），从句中缺少地点状语，用where引导。",
      },
      {
        id: "q008-3",
        question: "She told me the reason ___ she was late.",
        options: ["A. which", "B. that", "C. why", "D. where"],
        answer: "C",
        explanation: "先行词是reason（原因），用why引导定语从句。",
      },
    ],
    relatedCourse: "course4",
    relatedChapter: "基础语法",
    highlightWords: ["定语从句", "who", "which", "whose"],
    tags: ["定语从句", "从句", "四六级语法"],
    createdAt: "2024-02-25",
    status: "done",
    lastResult: "wrong",
    lastAttemptDate: "2024-02-28",
  },
]);

// 用户生词本模拟数据
export const USER_VOCABULARY = reactive([
  "virtual reality",
  "sustainable",
  "unprecedented",
  "deteriorate",
  "anthropogenic",
  "hieroglyphics",
]);

// 今日做题统计 - 响应式
export const todayStats = reactive({
  done: 0,
  correct: 0,
  date: new Date().toISOString().split("T")[0],
});

// 检查并重置每日统计
function checkDailyReset() {
  const today = new Date().toISOString().split("T")[0];
  if (todayStats.date !== today) {
    todayStats.done = 0;
    todayStats.correct = 0;
    todayStats.date = today;
  }
}

// 题库管理器
export const questionBankManager = {
  // 获取所有题目
  getAllQuestions() {
    return QUESTIONS;
  },

  // 根据筛选条件获取题目
  getFilteredQuestions(filters) {
    let result = [...QUESTIONS];

    // 按题型筛选
    if (filters.types && filters.types.length > 0) {
      result = result.filter((q) => filters.types.includes(q.type));
    }

    // 按难度筛选
    if (filters.difficulties && filters.difficulties.length > 0) {
      result = result.filter((q) =>
        filters.difficulties.includes(q.difficulty)
      );
    }

    // 按状态筛选
    if (filters.status && filters.status !== "all") {
      if (filters.status === "not-done") {
        result = result.filter((q) => q.status === "not-done");
      } else if (filters.status === "done") {
        result = result.filter((q) => q.status === "done");
      } else if (filters.status === "favorited") {
        result = result.filter((q) => q.favorited);
      }
    }

    // 只看当前课程相关
    if (filters.currentCourseOnly && filters.currentCourseId) {
      result = result.filter(
        (q) => q.relatedCourse === filters.currentCourseId
      );
    }

    // 包含生词的题目
    if (filters.includeVocabulary) {
      result = result.filter((q) => {
        return q.highlightWords.some((word) =>
          USER_VOCABULARY.some(
            (vocab) =>
              word.toLowerCase().includes(vocab.toLowerCase()) ||
              vocab.toLowerCase().includes(word.toLowerCase())
          )
        );
      });
    }

    // 关键词搜索
    if (filters.keyword && filters.keyword.trim()) {
      const keyword = filters.keyword.toLowerCase().trim();
      result = result.filter(
        (q) =>
          q.title.toLowerCase().includes(keyword) ||
          q.preview.toLowerCase().includes(keyword) ||
          q.tags.some((tag) => tag.toLowerCase().includes(keyword)) ||
          q.highlightWords.some((word) => word.toLowerCase().includes(keyword))
      );
    }

    return result;
  },

  // 获取错题列表
  getWrongQuestions() {
    return QUESTIONS.filter(
      (q) => q.status === "done" && q.lastResult === "wrong"
    );
  },

  // 获取已收藏题目
  getFavoritedQuestions() {
    return QUESTIONS.filter((q) => q.favorited);
  },

  // 获取题目详情
  getQuestionById(id) {
    return QUESTIONS.find((q) => q.id === id);
  },

  // 更新题目状态
  updateQuestionStatus(id, status, result) {
    const question = QUESTIONS.find((q) => q.id === id);
    if (question) {
      question.status = status;
      if (result) {
        question.lastResult = result;
        question.lastAttemptDate = new Date().toISOString().split("T")[0];
        // 更新今日统计
        checkDailyReset();
        todayStats.done++;
        if (result === "correct") {
          todayStats.correct++;
        }
      }
    }
  },

  // 切换收藏状态
  toggleFavorite(id) {
    const index = QUESTIONS.findIndex((q) => q.id === id);
    if (index !== -1) {
      // 直接修改响应式数组中的对象属性
      QUESTIONS[index].favorited = !QUESTIONS[index].favorited;
      return QUESTIONS[index].favorited;
    }
    return false;
  },

  // 获取统计数据
  getStatistics() {
    checkDailyReset();
    const total = QUESTIONS.length;
    const done = QUESTIONS.filter((q) => q.status === "done").length;
    const correct = QUESTIONS.filter(
      (q) => q.status === "done" && q.lastResult === "correct"
    ).length;
    const wrong = QUESTIONS.filter(
      (q) => q.status === "done" && q.lastResult === "wrong"
    ).length;
    const favorited = QUESTIONS.filter((q) => q.favorited).length;

    return {
      total,
      done,
      notDone: total - done,
      correct,
      wrong,
      accuracy: done > 0 ? Math.round((correct / done) * 100) : 0,
      favorited,
      masteredWords: USER_VOCABULARY.length,
      todayDone: todayStats.done,
      todayCorrect: todayStats.correct,
    };
  },
};

export default questionBankManager;
