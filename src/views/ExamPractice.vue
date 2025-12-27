<template>
  <div class="min-h-screen bg-gray-50">
    <!-- 顶部工具栏 -->
    <div class="bg-white shadow-sm border-b border-gray-200">
      <div
        class="container mx-auto px-4 py-3 flex justify-between items-center"
      >
        <div class="flex items-center gap-4">
          <button
            class="text-gray-600 hover:text-gray-800 flex items-center gap-2"
            @click="goBack"
          >
            <i class="fas fa-arrow-left" />
            <span>返回</span>
          </button>

          <!-- 考试模式显示试卷信息 -->
          <div v-if="mode === 'exam'" class="flex items-center gap-3 ml-4">
            <span class="text-lg font-bold text-gray-800">
              {{ paperInfo.name }}
            </span>
            <span
              class="px-3 py-1 bg-blue-100 text-blue-700 text-sm rounded-full"
            >
              {{ getCategoryLabel(paperInfo.category) }}
            </span>
            <span
              v-if="paperInfo.year"
              class="px-3 py-1 bg-gray-100 text-gray-700 text-sm rounded-full"
            >
              {{ paperInfo.year }}年
            </span>
          </div>

          <!-- 单题模式显示题目信息 -->
          <div v-else class="flex items-center gap-3 ml-4">
            <span class="text-lg font-bold text-gray-800">
              {{ questionInfo.sectionName || questionInfo.title }}
            </span>
            <span
              class="px-3 py-1 bg-blue-100 text-blue-700 text-sm rounded-full"
            >
              {{ getCategoryLabel(questionInfo.category) }}
            </span>
            <span
              class="px-3 py-1 bg-purple-100 text-purple-700 text-sm rounded-full"
            >
              {{ getSectionTypeLabel(currentQuestion.sectionType) }}
            </span>
          </div>
        </div>

        <div class="flex items-center gap-4">
          <!-- 收藏按钮 -->
          <button
            class="text-2xl transition-colors"
            :class="
              isFavorited
                ? 'text-yellow-400'
                : 'text-gray-300 hover:text-yellow-400'
            "
            @click="toggleFavorite"
          >
            <i :class="isFavorited ? 'fas fa-star' : 'far fa-star'" />
          </button>

          <!-- 全屏按钮 -->
          <button
            class="px-4 py-2 bg-gray-100 hover:bg-gray-200 rounded-lg transition-colors"
            @click="toggleFullscreen"
          >
            <i
              :class="isFullscreen ? 'fas fa-compress' : 'fas fa-expand'"
              class="mr-2"
            />
            {{ isFullscreen ? "退出全屏" : "全屏" }}
          </button>

          <!-- 提交按钮 -->
          <button
            class="px-6 py-2 bg-emerald-500 hover:bg-emerald-600 text-white rounded-lg transition-colors font-medium"
            :disabled="submitting"
            @click="submitAnswers"
          >
            <i class="fas fa-check mr-2" />
            {{ mode === "exam" ? "提交试卷" : "提交答案" }}
          </button>
        </div>
      </div>
    </div>

    <!-- 主内容区 -->
    <div class="container mx-auto px-4 py-6">
      <div
        v-if="loading"
        class="bg-white rounded-lg shadow-sm p-12 text-center"
      >
        <i class="fas fa-spinner fa-spin text-4xl text-emerald-500 mb-4" />
        <div class="text-gray-600">加载中...</div>
      </div>

      <div v-else class="flex flex-col gap-6">
        <!-- 主内容区域 -->
        <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
          <!-- 左侧：题目区域 -->
          <div
            class="bg-white rounded-lg shadow-sm p-6 overflow-y-auto"
            style="height: calc(100vh - 280px); min-height: 400px"
          >
            <!-- 当前题目的 section_name 和 title -->
            <div class="mb-6">
              <h2 class="text-xl font-bold text-gray-800 mb-2">
                {{ currentQuestion.sectionName }}
              </h2>
              <p
                v-if="currentQuestion.title"
                class="text-gray-600 leading-relaxed"
              >
                {{ currentQuestion.title }}
              </p>
            </div>

            <!-- 音频播放器 (听力题) -->
            <div
              v-if="currentQuestion.audioUrl"
              class="mb-6 p-4 bg-gradient-to-r from-blue-50 to-blue-100 rounded-lg"
            >
              <div class="flex items-center justify-between mb-3">
                <div class="flex items-center gap-2 text-blue-700">
                  <i class="fas fa-headphones text-xl" />
                  <span class="font-medium">听力材料</span>
                </div>
                <div class="text-sm text-blue-600">
                  {{
                    formatTime(
                      Math.max(
                        0,
                        audioCurrentTime - (currentQuestion.audioStartSec || 0)
                      )
                    )
                  }}
                  /
                  {{
                    formatTime(
                      (currentQuestion.audioEndSec || audioDuration) -
                        (currentQuestion.audioStartSec || 0)
                    )
                  }}
                </div>
              </div>

              <div class="flex items-center gap-3">
                <button
                  class="w-10 h-10 bg-blue-500 hover:bg-blue-600 text-white rounded-full flex items-center justify-center transition-colors"
                  @click="toggleAudioPlay"
                >
                  <i :class="audioPlaying ? 'fas fa-pause' : 'fas fa-play'" />
                </button>

                <div
                  class="flex-1 h-2 bg-blue-200 rounded-full overflow-hidden cursor-pointer"
                  @click="seekAudio"
                >
                  <div
                    class="h-full bg-blue-500 transition-all"
                    :style="{ width: audioProgress + '%' }"
                  />
                </div>

                <button
                  class="text-blue-600 hover:text-blue-700"
                  @click="resetAudio"
                >
                  <i class="fas fa-redo" />
                </button>
              </div>

              <audio
                ref="audioPlayer"
                :src="getResourceUrl(currentQuestion.audioUrl)"
                @timeupdate="handleAudioTimeUpdate"
                @loadedmetadata="handleAudioLoaded"
                @ended="handleAudioEnded"
              />
            </div>

            <!-- 材料文本 (阅读题等) -->
            <div v-if="currentQuestion.materialText" class="mb-6">
              <div class="prose max-w-none">
                <div
                  class="text-gray-700 leading-relaxed whitespace-pre-wrap"
                  v-html="formatMaterialText(currentQuestion.materialText)"
                />
              </div>
            </div>

            <!-- 材料图片 -->
            <div v-if="currentQuestion.materialImage" class="mb-6">
              <img
                :src="getResourceUrl(currentQuestion.materialImage)"
                alt="题目图片"
                class="w-full rounded-lg shadow-sm"
                @error="handleImageError"
              />
            </div>
          </div>

          <!-- 右侧：答题区域 -->
          <div
            class="bg-white rounded-lg shadow-sm p-6 flex flex-col"
            style="height: calc(100vh - 280px); min-height: 400px"
          >
            <!-- 小题列表 - 可滚动区域 -->
            <div class="flex-1 overflow-y-auto space-y-6">
              <div
                v-for="(item, index) in currentSubItems"
                :key="item.id"
                class="pb-6 border-b border-gray-200 last:border-0"
              >
                <!-- 题目编号和内容 -->
                <div class="mb-4">
                  <div class="flex items-start gap-3">
                    <span
                      class="flex-shrink-0 w-8 h-8 bg-emerald-100 text-emerald-700 rounded-full flex items-center justify-center text-sm font-bold"
                    >
                      {{ index + 1 }}
                    </span>
                    <div class="flex-1">
                      <p
                        class="text-gray-800 leading-relaxed"
                        v-html="item.content"
                      />
                      <!-- 调试信息：显示题型 -->
                      <span class="text-xs text-gray-400 ml-2"
                        >[{{ item.itemType || "unknown" }}]</span
                      >
                    </div>
                  </div>
                </div>

                <!-- 选择题 (单选: choice, 多选: multiple/multi_choice) -->
                <div
                  v-if="
                    item.itemType === 'choice' ||
                    item.itemType === 'multiple' ||
                    item.itemType === 'multi_choice'
                  "
                  class="ml-11 space-y-2"
                >
                  <label
                    v-for="option in parseOptions(item.options)"
                    :key="option.key"
                    class="flex items-start gap-3 p-3 rounded-lg hover:bg-gray-50 cursor-pointer transition-colors"
                    :class="{
                      'bg-emerald-50': isOptionSelected(item.id, option.key),
                    }"
                  >
                    <input
                      v-if="item.itemType === 'choice'"
                      :name="`question-${item.id}`"
                      type="radio"
                      :value="option.key"
                      :checked="isOptionSelected(item.id, option.key)"
                      class="mt-1 w-4 h-4 text-emerald-500 focus:ring-emerald-500"
                      @change="selectOption(item.id, option.key, 'single')"
                    />
                    <input
                      v-else
                      type="checkbox"
                      :value="option.key"
                      :checked="isOptionSelected(item.id, option.key)"
                      class="mt-1 w-4 h-4 text-emerald-500 focus:ring-emerald-500"
                      @change="selectOption(item.id, option.key, 'multiple')"
                    />
                    <span class="flex-1 text-gray-700">
                      <span class="font-medium">{{ option.key }}.</span>
                      {{ option.value }}
                    </span>
                  </label>
                </div>

                <!-- 填空题/简答题/写作题/翻译题 (支持 essay, text, fill, writing, translation, blank 类型) -->
                <div
                  v-else-if="
                    item.itemType === 'essay' ||
                    item.itemType === 'text' ||
                    item.itemType === 'fill' ||
                    item.itemType === 'blank' ||
                    item.itemType === 'writing' ||
                    item.itemType === 'translation'
                  "
                  class="ml-11"
                >
                  <input
                    v-if="item.itemType === 'fill' || item.itemType === 'blank'"
                    type="text"
                    :value="getUserAnswer(item.id)"
                    class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-emerald-500 focus:border-transparent"
                    placeholder="请输入答案"
                    @input="handleTextInput(item.id, $event.target.value)"
                  />
                  <textarea
                    v-else
                    :value="getUserAnswer(item.id)"
                    rows="8"
                    class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-emerald-500 focus:border-transparent resize-y"
                    :placeholder="getTextareaPlaceholder(item.itemType)"
                    @input="handleTextInput(item.id, $event.target.value)"
                  />
                </div>

                <!-- 插入题 (托福) - 选择 A/B/C/D 位置 -->
                <div v-else-if="item.itemType === 'insert'" class="ml-11">
                  <div class="text-sm text-gray-600 mb-3">
                    <i class="fas fa-info-circle mr-1" />
                    请选择句子应插入的位置 (A/B/C/D)
                  </div>
                  <div class="flex flex-wrap gap-3">
                    <button
                      v-for="pos in ['A', 'B', 'C', 'D']"
                      :key="pos"
                      class="px-6 py-3 rounded-lg font-medium transition-colors"
                      :class="
                        getUserAnswer(item.id) === pos
                          ? 'bg-emerald-500 text-white'
                          : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                      "
                      @click="handleInsertSelect(item.id, pos)"
                    >
                      位置 {{ pos }}
                    </button>
                  </div>
                  <div
                    v-if="getUserAnswer(item.id)"
                    class="mt-3 text-sm text-green-600"
                  >
                    <i class="fas fa-check-circle mr-1" />
                    已选择: 位置 {{ getUserAnswer(item.id) }}
                  </div>
                </div>

                <!-- 其他未识别的题型 - 提供默认输入框 -->
                <div v-else class="ml-11">
                  <textarea
                    :value="getUserAnswer(item.id)"
                    rows="4"
                    class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-emerald-500 focus:border-transparent resize-y"
                    placeholder="请输入答案..."
                    @input="handleTextInput(item.id, $event.target.value)"
                  />
                </div>
              </div>
            </div>

            <!-- 底部留白，确保导航按钮不遮挡内容 -->
            <div class="flex-shrink-0">
              <!-- 底部导航按钮 (考试模式) -->
              <div
                v-if="mode === 'exam' && questions.length > 1"
                class="mt-6 pt-4 border-t border-gray-200 flex justify-between"
              >
                <button
                  :disabled="currentQuestionIndex === 0"
                  class="px-6 py-2 bg-gray-100 hover:bg-gray-200 rounded-lg transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                  @click="prevQuestion"
                >
                  <i class="fas fa-chevron-left mr-2" />
                  上一题
                </button>
                <button
                  :disabled="currentQuestionIndex === questions.length - 1"
                  class="px-6 py-2 bg-gray-100 hover:bg-gray-200 rounded-lg transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                  @click="nextQuestion"
                >
                  下一题
                  <i class="fas fa-chevron-right ml-2" />
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- 考试模式：题目导航栏（按 sectionType 分组） -->
        <div
          v-if="mode === 'exam' && questions.length > 1"
          class="bg-white rounded-lg shadow-sm p-4"
        >
          <div class="flex items-center justify-between mb-4">
            <span class="text-sm font-bold text-gray-700">
              <i class="fas fa-list-ol mr-2 text-emerald-500" />
              题目导航
            </span>
            <span class="text-sm text-gray-600">
              当前: {{ currentQuestionIndex + 1 }} / {{ questions.length }}
            </span>
          </div>
          <!-- 按 sectionType 分组显示 -->
          <div class="space-y-4">
            <div
              v-for="section in groupedQuestions"
              :key="section.type"
              class="border-l-4 border-emerald-400 pl-3"
            >
              <div class="text-sm font-medium text-gray-700 mb-2">
                {{ getSectionTypeLabel(section.type) }}
                <span class="text-gray-400 ml-1"
                  >({{ section.items.length }}题)</span
                >
              </div>
              <div class="flex flex-wrap gap-2">
                <button
                  v-for="item in section.items"
                  :key="item.question.id"
                  class="w-9 h-9 rounded-lg flex items-center justify-center text-sm font-medium transition-colors"
                  :class="[
                    item.index === currentQuestionIndex
                      ? 'bg-emerald-500 text-white shadow-sm'
                      : hasAnswered(item.question.id)
                        ? 'bg-blue-100 text-blue-700 hover:bg-blue-200'
                        : 'bg-gray-100 text-gray-600 hover:bg-gray-200',
                  ]"
                  :title="item.question.sectionName"
                  @click="switchQuestion(item.index)"
                >
                  {{ item.index + 1 }}
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 提交确认 Modal -->
    <div
      v-if="showSubmitModal"
      class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50"
      @click.self="showSubmitModal = false"
    >
      <div class="bg-white rounded-lg shadow-xl p-6 max-w-md w-full mx-4">
        <h3 class="text-xl font-bold text-gray-800 mb-4">确认提交？</h3>
        <p class="text-gray-600 mb-6">
          {{
            mode === "exam"
              ? "确定要提交整份试卷吗？提交后将无法修改。"
              : "确定要提交答案吗？"
          }}
        </p>
        <div class="flex justify-end gap-3">
          <button
            class="px-4 py-2 bg-gray-100 hover:bg-gray-200 rounded-lg transition-colors"
            @click="showSubmitModal = false"
          >
            取消
          </button>
          <button
            class="px-4 py-2 bg-emerald-500 hover:bg-emerald-600 text-white rounded-lg transition-colors"
            @click="confirmSubmit"
          >
            确定提交
          </button>
        </div>
      </div>
    </div>

    <!-- 结果 Modal -->
    <div
      v-if="showResultModal"
      class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50"
    >
      <div class="bg-white rounded-lg shadow-xl p-6 max-w-md w-full mx-4">
        <div class="text-center">
          <div
            class="w-16 h-16 bg-emerald-100 rounded-full flex items-center justify-center mx-auto mb-4"
          >
            <i class="fas fa-check text-3xl text-emerald-500" />
          </div>
          <h3 class="text-2xl font-bold text-gray-800 mb-2">提交成功！</h3>
          <p class="text-gray-600 mb-4">
            得分:
            <span class="text-2xl font-bold text-emerald-600">{{
              resultScore
            }}</span>
            分
          </p>
          <p class="text-gray-600 mb-6">
            正确率:
            <span class="text-lg font-bold text-emerald-600"
              >{{ resultAccuracy }}%</span
            >
          </p>
          <div class="flex flex-col gap-3">
            <button
              class="w-full px-4 py-2 bg-blue-500 hover:bg-blue-600 text-white rounded-lg transition-colors"
              @click="
                showDetailModal = true;
                showResultModal = false;
              "
            >
              <i class="fas fa-list-alt mr-2" />
              查看详情
            </button>
            <button
              class="w-full px-4 py-2 bg-emerald-500 hover:bg-emerald-600 text-white rounded-lg transition-colors"
              @click="goBack"
            >
              返回题库
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 详情 Modal -->
    <div
      v-if="showDetailModal"
      class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4"
      @click.self="showDetailModal = false"
    >
      <div
        class="bg-white rounded-lg shadow-xl max-w-4xl w-full max-h-[90vh] overflow-hidden"
      >
        <div class="p-6 border-b border-gray-200">
          <div class="flex justify-between items-center">
            <h3 class="text-2xl font-bold text-gray-800">答题详情</h3>
            <button
              class="text-gray-400 hover:text-gray-600 text-2xl"
              @click="showDetailModal = false"
            >
              <i class="fas fa-times" />
            </button>
          </div>
        </div>
        <div class="p-6 overflow-y-auto" style="max-height: calc(90vh - 180px)">
          <div class="space-y-6">
            <div
              v-for="(detail, index) in resultDetails"
              :key="detail.subItemId"
              class="p-4 rounded-lg border"
              :class="
                detail.isCorrect
                  ? 'bg-green-50 border-green-200'
                  : 'bg-red-50 border-red-200'
              "
            >
              <div class="flex items-start justify-between mb-3">
                <div class="flex items-center gap-2">
                  <span class="text-lg font-bold text-gray-700"
                    >第 {{ index + 1 }} 题</span
                  >
                  <span
                    class="px-2 py-1 rounded text-sm font-medium"
                    :class="
                      detail.isCorrect
                        ? 'bg-green-100 text-green-700'
                        : 'bg-red-100 text-red-700'
                    "
                  >
                    <i
                      :class="
                        detail.isCorrect ? 'fas fa-check' : 'fas fa-times'
                      "
                    />
                    {{ detail.isCorrect ? "正确" : "错误" }}
                  </span>
                </div>
              </div>
              <div class="space-y-2 text-sm">
                <div class="flex items-start gap-2">
                  <span class="font-medium text-gray-600 min-w-20"
                    >题目内容:</span
                  >
                  <span class="text-gray-800" v-html="detail.content" />
                </div>
                <div class="flex items-start gap-2">
                  <span class="font-medium text-gray-600 min-w-20"
                    >你的答案:</span
                  >
                  <span
                    class="font-medium"
                    :class="
                      detail.isCorrect ? 'text-green-700' : 'text-red-700'
                    "
                  >
                    {{ detail.userAnswer || "未作答" }}
                  </span>
                </div>
                <div class="flex items-start gap-2">
                  <span class="font-medium text-gray-600 min-w-20"
                    >正确答案:</span
                  >
                  <span class="font-medium text-green-700">
                    {{ detail.correctAnswer }}
                  </span>
                </div>
                <div
                  v-if="detail.explanation"
                  class="flex items-start gap-2 mt-3 pt-3 border-t border-gray-200"
                >
                  <span class="font-medium text-gray-600 min-w-20"
                    >答案解析:</span
                  >
                  <span class="text-gray-700">{{ detail.explanation }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div class="p-6 border-t border-gray-200 bg-gray-50">
          <button
            class="w-full px-4 py-2 bg-emerald-500 hover:bg-emerald-600 text-white rounded-lg transition-colors"
            @click="goBack"
          >
            返回题库
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import {
  ref,
  reactive,
  computed,
  onMounted,
  onBeforeUnmount,
  watch,
} from "vue";
import { useRoute, useRouter } from "vue-router";
import questionApi from "@/api/question";

export default {
  name: "ExamPractice",
  setup() {
    const route = useRoute();
    const router = useRouter();

    // 模式
    const mode = ref(route.query.mode || "single"); // 'exam' 或 'single'

    // 数据
    const loading = ref(true);
    const submitting = ref(false);
    const paperInfo = reactive({});
    const questionInfo = reactive({});
    const questions = ref([]); // 所有题目
    const currentQuestionIndex = ref(0);
    const subItems = ref([]); // 当前题目的小题

    // 收藏状态
    const isFavorited = ref(false);

    // 全屏状态
    const isFullscreen = ref(false);

    // 答案
    const userAnswers = reactive({}); // { subItemId: answer }
    const insertPosition = reactive({}); // 插入题的位置

    // 音频
    const audioPlayer = ref(null);
    const audioPlaying = ref(false);
    const audioCurrentTime = ref(0);
    const audioDuration = ref(0);

    // Modal
    const showSubmitModal = ref(false);
    const showResultModal = ref(false);
    const showDetailModal = ref(false);
    const resultScore = ref(0);
    const resultAccuracy = ref(0);
    const resultDetails = ref([]);

    // 当前题目
    const currentQuestion = computed(() => {
      if (questions.value.length === 0) return {};
      return questions.value[currentQuestionIndex.value] || {};
    });

    // 当前小题
    const currentSubItems = computed(() => {
      const questionId = currentQuestion.value.id;
      if (!questionId) return [];
      return subItems.value.filter(
        (item) => item.parentQuestionId === questionId
      );
    });

    // 按 sectionType 分组的题目（考试模式使用）
    const groupedQuestions = computed(() => {
      if (questions.value.length === 0) return [];

      const groups = {};
      questions.value.forEach((q, index) => {
        const type = q.sectionType || "other";
        if (!groups[type]) {
          groups[type] = {
            type: type,
            items: [],
          };
        }
        groups[type].items.push({
          question: q,
          index: index,
        });
      });

      // 定义题型顺序
      const typeOrder = ["listening", "reading", "writing", "speaking"];
      return Object.values(groups).sort((a, b) => {
        const indexA = typeOrder.indexOf(a.type);
        const indexB = typeOrder.indexOf(b.type);
        if (indexA === -1 && indexB === -1) return 0;
        if (indexA === -1) return 1;
        if (indexB === -1) return -1;
        return indexA - indexB;
      });
    });

    // 音频进度（基于片段范围）
    const audioProgress = computed(() => {
      const startSec = currentQuestion.value.audioStartSec || 0;
      const endSec = currentQuestion.value.audioEndSec || audioDuration.value;
      const segmentDuration = endSec - startSec;

      if (segmentDuration === 0) return 0;

      const currentInSegment = audioCurrentTime.value - startSec;
      return Math.max(
        0,
        Math.min(100, (currentInSegment / segmentDuration) * 100)
      );
    });

    // 获取数据
    const fetchData = async () => {
      loading.value = true;
      try {
        if (mode.value === "exam") {
          const paperId = route.query.paperId || route.params.paperId;
          if (!paperId) {
            console.error("Missing paperId");
            return;
          }
          const response = await questionApi.getExamPaper(paperId);
          Object.assign(paperInfo, response.data.paper);
          questions.value = response.data.questions;
          subItems.value = response.data.subItems;
          isFavorited.value = response.data.isFavorited;
        } else {
          const questionId = route.query.questionId || route.params.questionId;
          if (!questionId) {
            console.error("Missing questionId");
            return;
          }
          const response = await questionApi.getQuestion(questionId);
          Object.assign(questionInfo, response.data.question);
          questions.value = [response.data.question];
          subItems.value = response.data.subItems;
          isFavorited.value = response.data.isFavorited;
        }
      } catch (error) {
        console.error("获取数据失败:", error);
        // 如果是认证错误，不要在这里处理跳转，由request拦截器统一处理
        // 这样可以避免刷新时误判
        if (error.response && error.response.status === 401) {
          // 401错误已经由拦截器处理，这里只记录日志
          console.log("认证失败，请检查登录状态");
        }
      } finally {
        loading.value = false;
      }
    };

    // 切换题目
    const switchQuestion = (index) => {
      currentQuestionIndex.value = index;
    };

    const prevQuestion = () => {
      if (currentQuestionIndex.value > 0) {
        currentQuestionIndex.value--;
      }
    };

    const nextQuestion = () => {
      if (currentQuestionIndex.value < questions.value.length - 1) {
        currentQuestionIndex.value++;
      }
    };

    // 判断是否已作答
    const hasAnswered = (questionId) => {
      const items = subItems.value.filter(
        (item) => item.parentQuestionId === questionId
      );
      return items.some((item) => userAnswers[item.id]);
    };

    // 选项解析
    const parseOptions = (options) => {
      try {
        return typeof options === "string" ? JSON.parse(options) : options;
      } catch {
        return [];
      }
    };

    // 选项选择
    const isOptionSelected = (subItemId, optionKey) => {
      const answer = userAnswers[subItemId];
      if (!answer) return false;
      if (Array.isArray(answer)) {
        return answer.includes(optionKey);
      }
      return answer === optionKey;
    };

    const selectOption = (subItemId, optionKey, type) => {
      if (type === "single") {
        userAnswers[subItemId] = optionKey;
      } else {
        if (!userAnswers[subItemId]) {
          userAnswers[subItemId] = [];
        }
        const index = userAnswers[subItemId].indexOf(optionKey);
        if (index > -1) {
          userAnswers[subItemId].splice(index, 1);
        } else {
          userAnswers[subItemId].push(optionKey);
        }
      }
    };

    // 文本输入
    const handleTextInput = (subItemId, value) => {
      userAnswers[subItemId] = value;
    };

    const getUserAnswer = (subItemId) => {
      return userAnswers[subItemId] || "";
    };

    // 插入题位置选择
    const handleInsertSelect = (subItemId, position) => {
      userAnswers[subItemId] = position;
      console.log(`[InsertSelect] itemId=${subItemId}, position=${position}`);
    };

    // 获取文本输入框的提示文字
    const getTextareaPlaceholder = (itemType) => {
      switch (itemType) {
        case "essay":
        case "writing":
          return "请输入作文/写作内容...";
        case "translation":
          return "请输入翻译内容...";
        case "text":
          return "请输入答案...";
        default:
          return "请输入答案...";
      }
    };

    // 格式化材料文本 (处理插入题标记)
    const formatMaterialText = (text) => {
      if (!text) return "";
      // 将 ■ 替换为可点击的标记
      return text.replace(
        /■/g,
        '<span class="insert-marker cursor-pointer text-blue-600 font-bold hover:bg-blue-100 px-1">[■]</span>'
      );
    };

    // 音频控制
    const toggleAudioPlay = () => {
      if (!audioPlayer.value) return;

      if (audioPlaying.value) {
        audioPlayer.value.pause();
      } else {
        // 如果设置了音频片段，跳转到开始位置
        if (currentQuestion.value.audioStartSec > 0) {
          audioPlayer.value.currentTime = currentQuestion.value.audioStartSec;
        }
        audioPlayer.value.play();
      }
      audioPlaying.value = !audioPlaying.value;
    };

    const resetAudio = () => {
      if (!audioPlayer.value) return;
      const startSec = currentQuestion.value.audioStartSec || 0;
      audioPlayer.value.currentTime = startSec;
      audioCurrentTime.value = startSec;
    };

    const seekAudio = (event) => {
      if (!audioPlayer.value) return;

      const startSec = currentQuestion.value.audioStartSec || 0;
      const endSec = currentQuestion.value.audioEndSec || audioDuration.value;
      const segmentDuration = endSec - startSec;

      const rect = event.currentTarget.getBoundingClientRect();
      const percent = (event.clientX - rect.left) / rect.width;
      const newTime = startSec + segmentDuration * percent;

      audioPlayer.value.currentTime = Math.max(
        startSec,
        Math.min(endSec, newTime)
      );
    };

    const handleAudioTimeUpdate = () => {
      if (!audioPlayer.value) return;
      audioCurrentTime.value = audioPlayer.value.currentTime;

      // 如果设置了结束时间，到达时暂停
      const endSec = currentQuestion.value.audioEndSec;
      if (endSec > 0 && audioCurrentTime.value >= endSec) {
        audioPlayer.value.pause();
        audioPlaying.value = false;
      }
    };

    const handleAudioLoaded = () => {
      if (!audioPlayer.value) return;
      audioDuration.value = audioPlayer.value.duration;

      // 设置初始播放位置为片段开始时间
      const startSec = currentQuestion.value.audioStartSec || 0;
      if (startSec > 0) {
        audioPlayer.value.currentTime = startSec;
        audioCurrentTime.value = startSec;
      }
    };

    const handleAudioEnded = () => {
      audioPlaying.value = false;
    };

    const formatTime = (seconds) => {
      const mins = Math.floor(seconds / 60);
      const secs = Math.floor(seconds % 60);
      return `${mins}:${secs.toString().padStart(2, "0")}`;
    };

    // 图片错误处理
    const handleImageError = (event) => {
      event.target.src = "/placeholder-image.png";
    };

    // 资源URL处理
    const getResourceUrl = (path) => {
      if (!path) return "";

      console.log("[Resource Debug] Original path:", path);

      // 如果已经是完整URL，直接返回
      if (path.startsWith("http://") || path.startsWith("https://")) {
        console.log("[Resource Debug] Full URL detected:", path);
        return path;
      }

      // 如果是相对路径，添加后端服务器地址
      const baseURL = `${window.location.protocol}//${window.location.hostname}:8080`;

      // 如果路径以 /uploads 开头，需要转换为 /api/files
      let finalPath = path;
      if (path.startsWith("/uploads/")) {
        finalPath = path.replace("/uploads/", "/api/files/");
      } else if (path.startsWith("uploads/")) {
        finalPath = "/api/files/" + path.replace("uploads/", "");
      }

      const finalUrl = path.startsWith("/")
        ? `${baseURL}${finalPath}`
        : `${baseURL}/${finalPath}`;
      console.log("[Resource Debug] Final URL:", finalUrl);

      return finalUrl;
    };

    // 全屏
    const toggleFullscreen = () => {
      if (!document.fullscreenElement) {
        document.documentElement.requestFullscreen();
        isFullscreen.value = true;
      } else {
        document.exitFullscreen();
        isFullscreen.value = false;
      }
    };

    // 收藏
    const toggleFavorite = async () => {
      try {
        const id = mode.value === "exam" ? paperInfo.id : questionInfo.id;
        if (isFavorited.value) {
          await questionApi.removeFavorite(
            mode.value === "exam" ? "paper" : "question",
            id
          );
        } else {
          await questionApi.addFavorite(
            mode.value === "exam" ? "paper" : "question",
            id
          );
        }
        isFavorited.value = !isFavorited.value;
      } catch (error) {
        console.error("收藏操作失败:", error);
      }
    };

    // 提交答案
    const submitAnswers = () => {
      showSubmitModal.value = true;
    };

    const confirmSubmit = async () => {
      showSubmitModal.value = false;
      submitting.value = true;

      try {
        const data = {
          mode: mode.value,
          paperId: mode.value === "exam" ? paperInfo.id : questionInfo.paperId,
          questionId: questionInfo.id,
          answers: Object.keys(userAnswers).map((subItemId) => ({
            subItemId: parseInt(subItemId),
            answer: userAnswers[subItemId],
          })),
        };

        const response = await questionApi.submitAnswers(data);
        resultScore.value = response.data.score;
        resultAccuracy.value = response.data.accuracy;
        resultDetails.value = response.data.details || [];
        showResultModal.value = true;
      } catch (error) {
        console.error("提交失败:", error);
      } finally {
        submitting.value = false;
      }
    };

    // 返回
    const goBack = () => {
      if (mode.value === "exam") {
        // 考试模式返回考试列表
        router.push({ path: "/question-bank", query: { tab: "exam" } });
      } else {
        // 单题模式返回单题列表
        router.push({ path: "/question-bank", query: { tab: "single" } });
      }
    };

    // 标签
    const getCategoryLabel = (value) => {
      const labels = {
        CET4: "四级",
        CET6: "六级",
        KY: "考研",
        IELTS: "雅思",
        TOEFL: "托福",
      };
      return labels[value] || value;
    };

    const getSectionTypeLabel = (value) => {
      const labels = {
        listening: "听力",
        reading: "阅读",
        writing: "写作",
        speaking: "口语",
      };
      return labels[value] || value;
    };

    // 监听题目切换，重置音频
    watch(currentQuestionIndex, () => {
      if (audioPlayer.value) {
        audioPlayer.value.pause();
        audioPlaying.value = false;
        audioCurrentTime.value = 0;
      }
    });

    // 监听全屏变化
    const handleFullscreenChange = () => {
      isFullscreen.value = !!document.fullscreenElement;
    };

    onMounted(() => {
      fetchData();
      document.addEventListener("fullscreenchange", handleFullscreenChange);
    });

    onBeforeUnmount(() => {
      document.removeEventListener("fullscreenchange", handleFullscreenChange);
      if (audioPlayer.value) {
        audioPlayer.value.pause();
      }
    });

    return {
      mode,
      loading,
      submitting,
      paperInfo,
      questionInfo,
      questions,
      currentQuestionIndex,
      currentQuestion,
      currentSubItems,
      groupedQuestions,
      isFavorited,
      isFullscreen,
      userAnswers,
      insertPosition,
      audioPlayer,
      audioPlaying,
      audioCurrentTime,
      audioDuration,
      audioProgress,
      showSubmitModal,
      showResultModal,
      showDetailModal,
      resultScore,
      resultAccuracy,
      resultDetails,
      switchQuestion,
      prevQuestion,
      nextQuestion,
      hasAnswered,
      parseOptions,
      isOptionSelected,
      getResourceUrl,
      selectOption,
      handleTextInput,
      handleInsertSelect,
      getUserAnswer,
      getTextareaPlaceholder,
      formatMaterialText,
      toggleAudioPlay,
      resetAudio,
      seekAudio,
      handleAudioTimeUpdate,
      handleAudioLoaded,
      handleAudioEnded,
      formatTime,
      handleImageError,
      toggleFullscreen,
      toggleFavorite,
      submitAnswers,
      confirmSubmit,
      goBack,
      getCategoryLabel,
      getSectionTypeLabel,
    };
  },
};
</script>

<style scoped>
.prose {
  font-size: 1rem;
  line-height: 1.75;
}

:deep(.insert-marker) {
  display: inline-block;
  margin: 0 2px;
  padding: 2px 6px;
  border-radius: 4px;
}
</style>
