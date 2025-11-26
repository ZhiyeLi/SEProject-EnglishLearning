<template>
  <div class="min-h-screen bg-gray-50 flex flex-col">
    <!-- 导航栏 -->
    <NavBar :nav-items="navItems">
      <template #actions>
        <button
          class="text-gray-600 hover:text-emerald-600 p-2 rounded-full hover:bg-emerald-50 transition-colors relative group"
          @click="showSuggestionsModal = true"
        >
          <i class="fas fa-lightbulb text-lg" />
          <span
            class="absolute -top-10 right-0 bg-gray-800 text-white text-sm px-2 py-1 rounded opacity-0 group-hover:opacity-100 transition-opacity whitespace-nowrap"
          >学习建议</span>
        </button>
        <button
          class="text-gray-600 hover:text-emerald-600 p-2 rounded-full hover:bg-emerald-50 transition-colors relative group"
          @click="gotoSettings"
        >
          <i class="fas fa-cog text-lg" />
          <span
            class="absolute -top-10 right-0 bg-gray-800 text-white text-sm px-2 py-1 rounded opacity-0 group-hover:opacity-100 transition-opacity whitespace-nowrap"
          >设置</span>
        </button>
        <button
          class="relative ml-2 text-gray-600 hover:text-emerald-600 p-2 rounded-full hover:bg-emerald-50 transition-colors"
        >
          <i class="fas fa-bell text-lg" />
          <span
            class="absolute -top-1 -right-1 bg-red-500 text-white text-xs rounded-full h-4 w-4 flex items-center justify-center animate-pulse"
          >3</span>
        </button>
      </template>
    </NavBar>

    <!-- 主内容区 -->
    <main class="flex-grow flex">
      <!-- 左侧筛选侧边栏 -->
      <aside
        class="w-72 bg-white border-r border-gray-200 shadow-sm p-5 overflow-y-auto flex-shrink-0 hidden lg:block"
      >
        <h3 class="text-lg font-semibold text-gray-800 mb-4 flex items-center">
          <i class="fas fa-filter text-emerald-500 mr-2" />
          筛选条件
        </h3>

        <!-- 关联筛选 -->
        <div class="mb-6">
          <h4 class="text-sm font-medium text-gray-700 mb-3">
            关联筛选
          </h4>
          <div class="space-y-2">
            <label class="flex items-center cursor-pointer group">
              <input
                v-model="filters.relatedToCourse"
                type="checkbox"
                class="w-4 h-4 text-emerald-600 border-gray-300 rounded focus:ring-emerald-500"
              >
              <span
                class="ml-2 text-sm text-gray-600 group-hover:text-emerald-600"
              >只看当前课程相关</span>
            </label>
            <label class="flex items-center cursor-pointer group">
              <input
                v-model="filters.includeMyVocab"
                type="checkbox"
                class="w-4 h-4 text-emerald-600 border-gray-300 rounded focus:ring-emerald-500"
              >
              <span
                class="ml-2 text-sm text-gray-600 group-hover:text-emerald-600"
              >包含我的生词</span>
            </label>
          </div>
        </div>

        <!-- 题型筛选 -->
        <div class="mb-6">
          <h4 class="text-sm font-medium text-gray-700 mb-3">
            题型
          </h4>
          <div class="space-y-2">
            <label
              v-for="type in questionTypes"
              :key="type.value"
              class="flex items-center cursor-pointer group"
            >
              <input
                v-model="filters.types"
                type="checkbox"
                :value="type.value"
                class="w-4 h-4 text-emerald-600 border-gray-300 rounded focus:ring-emerald-500"
              >
              <span
                class="ml-2 text-sm text-gray-600 group-hover:text-emerald-600"
              >{{ type.label }}</span>
            </label>
          </div>
        </div>

        <!-- 难度筛选 -->
        <div class="mb-6">
          <h4 class="text-sm font-medium text-gray-700 mb-3">
            难度/级别
          </h4>
          <div class="space-y-2">
            <label
              v-for="level in difficultyLevels"
              :key="level.value"
              class="flex items-center cursor-pointer group"
            >
              <input
                v-model="filters.difficulty"
                type="checkbox"
                :value="level.value"
                class="w-4 h-4 text-emerald-600 border-gray-300 rounded focus:ring-emerald-500"
              >
              <span
                class="ml-2 text-sm text-gray-600 group-hover:text-emerald-600"
              >{{ level.label }}</span>
            </label>
          </div>
        </div>

        <!-- 状态筛选 -->
        <div class="mb-6">
          <h4 class="text-sm font-medium text-gray-700 mb-3">
            做题状态
          </h4>
          <div class="space-y-2">
            <label
              v-for="status in statusOptions"
              :key="status.value"
              class="flex items-center cursor-pointer group"
            >
              <input
                v-model="filters.status"
                type="checkbox"
                :value="status.value"
                class="w-4 h-4 text-emerald-600 border-gray-300 rounded focus:ring-emerald-500"
              >
              <span
                class="ml-2 text-sm text-gray-600 group-hover:text-emerald-600"
              >{{ status.label }}</span>
            </label>
          </div>
        </div>

        <!-- 重置筛选 -->
        <button
          class="w-full py-2 text-sm text-gray-600 hover:text-emerald-600 border border-gray-200 rounded-lg hover:border-emerald-300 transition-all"
          @click="resetFilters"
        >
          <i class="fas fa-undo mr-2" />重置筛选
        </button>
      </aside>

      <!-- 右侧主内容区 -->
      <div class="flex-grow p-6 overflow-y-auto">
        <div class="max-w-5xl mx-auto">
          <!-- 顶部统计栏 -->
          <div
            class="bg-white rounded-xl shadow-sm border border-gray-200 p-5 mb-6"
          >
            <div
              class="flex flex-col md:flex-row justify-between items-start md:items-center gap-4"
            >
              <!-- 全局搜索框 -->
              <div class="relative flex-grow max-w-xl">
                <input
                  v-model="searchQuery"
                  type="text"
                  placeholder="搜索题目、语法点或课程名称..."
                  class="w-full pl-10 pr-4 py-3 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent transition-all text-base"
                  @input="debouncedSearch"
                >
                <i
                  class="fas fa-search absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 text-lg"
                />
                <button
                  v-if="searchQuery"
                  class="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400 hover:text-gray-600"
                  @click="searchQuery = ''"
                >
                  <i class="fas fa-times" />
                </button>
              </div>

              <!-- 简要统计 -->
              <div class="flex items-center space-x-6 text-sm">
                <div class="flex items-center">
                  <i class="fas fa-check-circle text-emerald-500 mr-2" />
                  <span class="text-gray-600">今日做题：</span>
                  <span class="font-semibold text-emerald-600">{{
                    stats.todayCount
                  }}</span>
                </div>
                <div class="flex items-center">
                  <i class="fas fa-chart-pie text-blue-500 mr-2" />
                  <span class="text-gray-600">正确率：</span>
                  <span class="font-semibold text-blue-600">{{ stats.accuracy }}%</span>
                </div>
                <div class="flex items-center">
                  <i class="fas fa-book text-purple-500 mr-2" />
                  <span class="text-gray-600">已掌握单词：</span>
                  <span class="font-semibold text-purple-600">{{
                    stats.masteredWords
                  }}</span>
                </div>
              </div>
            </div>
          </div>

          <!-- 移动端筛选按钮 -->
          <button
            class="lg:hidden w-full mb-4 py-3 bg-white border border-gray-200 rounded-lg text-gray-600 flex items-center justify-center"
            @click="showMobileFilter = true"
          >
            <i class="fas fa-filter mr-2" />筛选条件
            <span
              v-if="activeFilterCount > 0"
              class="ml-2 bg-emerald-500 text-white text-xs px-2 py-0.5 rounded-full"
            >
              {{ activeFilterCount }}
            </span>
          </button>

          <!-- 筛选结果统计 -->
          <div class="flex items-center justify-between mb-4">
            <p class="text-sm text-gray-600">
              共找到
              <span class="font-semibold text-emerald-600">{{
                filteredQuestions.length
              }}</span>
              道题目
            </p>
            <div class="flex items-center gap-2">
              <span class="text-sm text-gray-500">排序：</span>
              <select
                v-model="sortBy"
                class="text-sm border border-gray-200 rounded-lg px-3 py-1.5 focus:outline-none focus:ring-2 focus:ring-emerald-500"
              >
                <option value="default">
                  默认排序
                </option>
                <option value="difficulty-asc">
                  难度由低到高
                </option>
                <option value="difficulty-desc">
                  难度由高到低
                </option>
                <option value="latest">
                  最新添加
                </option>
              </select>
            </div>
          </div>

          <!-- 题目列表 -->
          <div class="space-y-4">
            <div
              v-for="question in paginatedQuestions"
              :key="question.id"
              class="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden transform transition-all duration-300 hover:shadow-md hover:-translate-y-0.5"
            >
              <!-- 卡片头部 -->
              <div
                class="px-5 py-3 bg-gray-50 border-b border-gray-100 flex items-center justify-between flex-wrap gap-2"
              >
                <div class="flex items-center flex-wrap gap-2">
                  <!-- 题型标签 -->
                  <span
                    :class="[
                      'px-2 py-1 text-xs font-medium rounded-full',
                      getTypeClass(question.type),
                    ]"
                  >
                    {{ getTypeLabel(question.type) }}
                  </span>
                  <!-- 难度标签 -->
                  <span
                    :class="[
                      'px-2 py-1 text-xs font-medium rounded-full',
                      getDifficultyClass(question.difficulty),
                    ]"
                  >
                    {{ getDifficultyLabel(question.difficulty) }}
                  </span>
                  <!-- 课程关联 -->
                  <span
                    v-if="question.relatedCourse"
                    class="flex items-center text-xs text-gray-500"
                  >
                    <i class="fas fa-tag text-emerald-400 mr-1" />
                    关联课程：{{ question.relatedCourse }}
                  </span>
                </div>
                <!-- 难度星级 -->
                <div class="flex items-center">
                  <i
                    v-for="star in 5"
                    :key="star"
                    :class="[
                      'fas fa-star text-sm',
                      star <= question.difficulty
                        ? 'text-yellow-400'
                        : 'text-gray-200',
                    ]"
                  />
                </div>
              </div>

              <!-- 卡片内容 -->
              <div class="p-5">
                <!-- 标题预览 -->
                <h3 class="text-lg font-medium text-gray-800 mb-2 line-clamp-2">
                  <span v-html="highlightVocab(question.title)" />
                </h3>
                <!-- 题目描述 -->
                <p class="text-gray-600 text-sm mb-3 line-clamp-2">
                  <span v-html="highlightVocab(question.preview)" />
                </p>
                <!-- 知识点标签 -->
                <div class="flex flex-wrap gap-1 mb-4">
                  <span
                    v-for="tag in question.tags"
                    :key="tag"
                    class="px-2 py-0.5 text-xs bg-gray-100 text-gray-600 rounded"
                  >
                    {{ tag }}
                  </span>
                </div>
              </div>

              <!-- 卡片底部 -->
              <div
                class="px-5 py-3 bg-gray-50 border-t border-gray-100 flex items-center justify-between flex-wrap gap-3"
              >
                <!-- 做题状态 -->
                <div class="flex items-center text-sm">
                  <span
                    v-if="question.userStatus === 'done'"
                    class="flex items-center text-emerald-600"
                  >
                    <i class="fas fa-check-circle mr-1" />上次正确
                  </span>
                  <span
                    v-else-if="question.userStatus === 'wrong'"
                    class="flex items-center text-red-500"
                  >
                    <i class="fas fa-times-circle mr-1" />上次错误 ({{
                      question.lastPracticeTime
                    }})
                  </span>
                  <span
                    v-else
                    class="flex items-center text-gray-400"
                  >
                    <i class="fas fa-circle mr-1" />未做过
                  </span>
                </div>

                <!-- 操作按钮 -->
                <div class="flex items-center gap-2">
                  <button
                    class="px-4 py-2 text-sm bg-emerald-500 text-white rounded-lg hover:bg-emerald-600 transition-all shadow-sm hover:shadow"
                    @click="startPractice(question)"
                  >
                    <i class="fas fa-play mr-1" />开始练习
                  </button>
                  <button
                    class="p-2 text-gray-400 hover:text-blue-500 transition-colors"
                    title="加入计划"
                    @click="addToPlan(question)"
                  >
                    <i class="fas fa-calendar-plus" />
                  </button>
                  <button
                    :class="[
                      'p-2 transition-colors',
                      question.isFavorite
                        ? 'text-red-500'
                        : 'text-gray-400 hover:text-red-500',
                    ]"
                    title="收藏"
                    @click="toggleFavorite(question)"
                  >
                    <i
                      :class="
                        question.isFavorite ? 'fas fa-heart' : 'far fa-heart'
                      "
                    />
                  </button>
                </div>
              </div>
            </div>
          </div>

          <!-- 空状态 -->
          <div
            v-if="filteredQuestions.length === 0"
            class="text-center py-16 bg-white rounded-xl shadow-sm border border-gray-200"
          >
            <i class="fas fa-search text-gray-300 text-6xl mb-4" />
            <p class="text-gray-500 text-lg mb-2">
              没有找到匹配的题目
            </p>
            <p class="text-gray-400 text-sm">
              请尝试调整筛选条件或搜索关键词
            </p>
            <button
              class="mt-4 text-emerald-600 hover:text-emerald-700"
              @click="resetFilters"
            >
              <i class="fas fa-undo mr-1" />重置筛选条件
            </button>
          </div>

          <!-- 分页 -->
          <div
            v-if="filteredQuestions.length > 0"
            class="mt-6 flex items-center justify-center gap-2"
          >
            <button
              :disabled="currentPage === 1"
              class="px-4 py-2 border border-gray-200 rounded-lg text-gray-600 hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
              @click="currentPage--"
            >
              <i class="fas fa-chevron-left" />
            </button>
            <span class="px-4 py-2 text-gray-600">
              {{ currentPage }} / {{ totalPages }}
            </span>
            <button
              :disabled="currentPage === totalPages"
              class="px-4 py-2 border border-gray-200 rounded-lg text-gray-600 hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
              @click="currentPage++"
            >
              <i class="fas fa-chevron-right" />
            </button>
          </div>
        </div>
      </div>
    </main>

    <!-- 练习模态框 -->
    <teleport to="body">
      <div
        v-if="showPracticeModal"
        class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4"
      >
        <div
          class="bg-white rounded-2xl shadow-2xl w-full max-w-5xl max-h-[90vh] overflow-hidden flex flex-col animate-fadeIn"
        >
          <!-- 模态框头部 -->
          <div
            class="px-6 py-4 border-b border-gray-200 flex items-center justify-between bg-gradient-to-r from-emerald-50 to-blue-50"
          >
            <div class="flex items-center">
              <span
                :class="[
                  'px-3 py-1 text-sm font-medium rounded-full mr-3',
                  getTypeClass(currentQuestion?.type),
                ]"
              >
                {{ getTypeLabel(currentQuestion?.type) }}
              </span>
              <h2 class="text-xl font-bold text-gray-800">
                {{ currentQuestion?.title }}
              </h2>
            </div>
            <button
              class="text-gray-400 hover:text-gray-600 p-2"
              @click="closePracticeModal"
            >
              <i class="fas fa-times text-xl" />
            </button>
          </div>

          <!-- 模态框内容 - 分屏设计 -->
          <div class="flex-grow flex overflow-hidden">
            <!-- 左侧：阅读材料/听力播放器 -->
            <div
              class="w-1/2 p-6 border-r border-gray-200 overflow-y-auto bg-gray-50"
            >
              <h3 class="text-lg font-semibold text-gray-800 mb-4">
                <i class="fas fa-book-open text-emerald-500 mr-2" />阅读材料
              </h3>
              <div
                class="prose prose-sm max-w-none text-gray-700 leading-relaxed select-text"
                @mouseup="handleTextSelection"
              >
                <p v-html="currentQuestion?.content" />
              </div>

              <!-- 划词翻译弹窗 -->
              <div
                v-if="showWordPopup"
                :style="{
                  top: wordPopupPosition.y + 'px',
                  left: wordPopupPosition.x + 'px',
                }"
                class="absolute bg-white rounded-lg shadow-xl border border-gray-200 p-4 z-50 min-w-[200px]"
              >
                <div class="flex items-center justify-between mb-2">
                  <span class="font-bold text-gray-800">{{
                    selectedWord
                  }}</span>
                  <button
                    class="text-gray-400 hover:text-gray-600"
                    @click="showWordPopup = false"
                  >
                    <i class="fas fa-times" />
                  </button>
                </div>
                <p class="text-sm text-gray-600 mb-3">
                  {{ wordTranslation }}
                </p>
                <button
                  class="w-full py-2 bg-emerald-500 text-white text-sm rounded-lg hover:bg-emerald-600 transition-all"
                  @click="addToVocab"
                >
                  <i class="fas fa-plus mr-1" />加入单词打卡
                </button>
              </div>
            </div>

            <!-- 右侧：题目区域 -->
            <div class="w-1/2 p-6 overflow-y-auto">
              <h3 class="text-lg font-semibold text-gray-800 mb-4">
                <i class="fas fa-question-circle text-blue-500 mr-2" />题目
              </h3>

              <!-- 题目列表 -->
              <div class="space-y-6">
                <div
                  v-for="(item, index) in currentQuestion?.questions"
                  :key="index"
                  class="bg-gray-50 rounded-lg p-4"
                >
                  <p class="font-medium text-gray-800 mb-3">
                    {{ index + 1 }}. {{ item.question }}
                  </p>
                  <div class="space-y-2">
                    <label
                      v-for="(option, optIndex) in item.options"
                      :key="optIndex"
                      :class="[
                        'flex items-center p-3 rounded-lg cursor-pointer transition-all',
                        getUserAnswer(index) === optIndex
                          ? showAnswer
                            ? optIndex === item.correctAnswer
                              ? 'bg-emerald-100 border-2 border-emerald-500'
                              : 'bg-red-100 border-2 border-red-500'
                            : 'bg-blue-100 border-2 border-blue-500'
                          : showAnswer && optIndex === item.correctAnswer
                            ? 'bg-emerald-100 border-2 border-emerald-500'
                            : 'bg-white border border-gray-200 hover:border-blue-300',
                      ]"
                    >
                      <input
                        v-model="userAnswers[index]"
                        type="radio"
                        :name="'question-' + index"
                        :value="optIndex"
                        :disabled="showAnswer"
                        class="w-4 h-4 text-blue-600 border-gray-300 focus:ring-blue-500"
                      >
                      <span class="ml-3 text-gray-700">{{ String.fromCharCode(65 + optIndex) }}.
                        {{ option }}</span>
                      <i
                        v-if="showAnswer && optIndex === item.correctAnswer"
                        class="fas fa-check-circle text-emerald-500 ml-auto"
                      />
                      <i
                        v-if="
                          showAnswer &&
                            getUserAnswer(index) === optIndex &&
                            optIndex !== item.correctAnswer
                        "
                        class="fas fa-times-circle text-red-500 ml-auto"
                      />
                    </label>
                  </div>

                  <!-- 解析 -->
                  <div
                    v-if="showAnswer"
                    class="mt-4 p-4 bg-blue-50 rounded-lg border border-blue-200"
                  >
                    <h4 class="font-medium text-blue-800 mb-2">
                      <i class="fas fa-lightbulb mr-1" />答案解析
                    </h4>
                    <p class="text-sm text-gray-700">
                      {{ item.explanation }}
                    </p>
                    <div
                      v-if="item.relatedGrammar"
                      class="mt-2"
                    >
                      <a
                        href="#"
                        class="text-sm text-emerald-600 hover:text-emerald-700 flex items-center"
                      >
                        <i class="fas fa-video mr-1" />查看相关语法点微课
                      </a>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 模态框底部 -->
          <div
            class="px-6 py-4 border-t border-gray-200 flex items-center justify-between bg-gray-50"
          >
            <div class="text-sm text-gray-500">
              <i class="fas fa-clock mr-1" />用时：{{
                formatTime(practiceTime)
              }}
            </div>
            <div class="flex items-center gap-3">
              <button
                v-if="!showAnswer"
                :disabled="!allAnswered"
                class="px-6 py-2 bg-emerald-500 text-white rounded-lg hover:bg-emerald-600 disabled:bg-gray-300 disabled:cursor-not-allowed transition-all"
                @click="submitAnswers"
              >
                <i class="fas fa-paper-plane mr-2" />提交答案
              </button>
              <button
                v-else
                class="px-6 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600 transition-all"
                @click="closePracticeModal"
              >
                <i class="fas fa-check mr-2" />完成练习
              </button>
            </div>
          </div>
        </div>
      </div>
    </teleport>

    <!-- 加入计划模态框 -->
    <teleport to="body">
      <div
        v-if="showAddPlanModal"
        class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4"
      >
        <div
          class="bg-white rounded-xl shadow-xl w-full max-w-md p-6 animate-fadeIn"
        >
          <div class="flex items-center justify-between mb-4">
            <h3 class="text-lg font-semibold text-gray-800">
              <i
                class="fas fa-calendar-plus text-emerald-500 mr-2"
              />加入学习计划
            </h3>
            <button
              class="text-gray-400 hover:text-gray-600"
              @click="showAddPlanModal = false"
            >
              <i class="fas fa-times" />
            </button>
          </div>

          <div class="mb-4">
            <label class="block text-sm font-medium text-gray-700 mb-2">选择日期</label>
            <input
              v-model="planDate"
              type="date"
              class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-emerald-500"
            >
          </div>

          <div class="mb-4">
            <label class="block text-sm font-medium text-gray-700 mb-2">备注（可选）</label>
            <textarea
              v-model="planNote"
              rows="3"
              placeholder="例如：复习阅读长难句"
              class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-emerald-500 resize-none"
            />
          </div>

          <div class="flex justify-end gap-3">
            <button
              class="px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50"
              @click="showAddPlanModal = false"
            >
              取消
            </button>
            <button
              class="px-4 py-2 bg-emerald-500 text-white rounded-lg hover:bg-emerald-600"
              @click="confirmAddToPlan"
            >
              确认添加
            </button>
          </div>
        </div>
      </div>
    </teleport>

    <!-- 移动端筛选抽屉 -->
    <teleport to="body">
      <div
        v-if="showMobileFilter"
        class="fixed inset-0 bg-black bg-opacity-50 z-50 lg:hidden"
        @click="showMobileFilter = false"
      >
        <div
          class="absolute left-0 top-0 bottom-0 w-80 bg-white shadow-xl overflow-y-auto"
          @click.stop
        >
          <div class="p-5">
            <div class="flex items-center justify-between mb-4">
              <h3 class="text-lg font-semibold text-gray-800">
                筛选条件
              </h3>
              <button
                class="text-gray-400 hover:text-gray-600"
                @click="showMobileFilter = false"
              >
                <i class="fas fa-times" />
              </button>
            </div>
            <!-- 复用筛选内容 -->
            <!-- 这里省略，实际项目中应该抽取为组件 -->
          </div>
        </div>
      </div>
    </teleport>

    <!-- 右侧悬浮错题本按钮 -->
    <div class="fixed right-6 top-1/2 transform -translate-y-1/2 z-40">
      <button
        class="group relative bg-gradient-to-br from-red-500 to-orange-500 text-white p-4 rounded-full shadow-lg hover:shadow-xl transform hover:scale-110 transition-all duration-300"
        @click="toggleErrorBook"
      >
        <i class="fas fa-times-circle text-xl" />
        <span
          v-if="wrongQuestions.length > 0"
          class="absolute -top-2 -right-2 bg-yellow-400 text-red-800 text-xs font-bold rounded-full h-6 w-6 flex items-center justify-center shadow"
        >{{ wrongQuestions.length }}</span>
        <span
          class="absolute right-full mr-3 bg-gray-800 text-white text-sm px-3 py-2 rounded-lg opacity-0 group-hover:opacity-100 transition-opacity whitespace-nowrap shadow-lg"
        >
          错题本 ({{ wrongQuestions.length }})
        </span>
      </button>
    </div>

    <!-- 错题本侧边抽屉 -->
    <teleport to="body">
      <transition name="slide-right">
        <div
          v-if="showErrorBook"
          class="fixed inset-0 z-50"
          @click="showErrorBook = false"
        >
          <!-- 遮罩层 -->
          <div class="absolute inset-0 bg-black bg-opacity-40" />

          <!-- 侧边抽屉 -->
          <div
            class="absolute right-0 top-0 bottom-0 w-96 bg-white shadow-2xl overflow-hidden flex flex-col"
            @click.stop
          >
            <!-- 抽屉头部 -->
            <div
              class="bg-gradient-to-r from-red-500 to-orange-500 text-white px-6 py-5"
            >
              <div class="flex items-center justify-between">
                <div class="flex items-center">
                  <i class="fas fa-times-circle text-2xl mr-3" />
                  <div>
                    <h3 class="text-lg font-bold">
                      错题本
                    </h3>
                    <p class="text-red-100 text-sm">
                      共 {{ wrongQuestions.length }} 道错题
                    </p>
                  </div>
                </div>
                <button
                  class="text-white/80 hover:text-white p-2 rounded-full hover:bg-white/20 transition-colors"
                  @click="showErrorBook = false"
                >
                  <i class="fas fa-times text-lg" />
                </button>
              </div>
            </div>

            <!-- 错题列表 -->
            <div class="flex-1 overflow-y-auto p-4">
              <div
                v-if="wrongQuestions.length === 0"
                class="flex flex-col items-center justify-center h-full text-gray-400"
              >
                <i class="fas fa-check-circle text-6xl mb-4 text-emerald-300" />
                <p class="text-lg font-medium text-gray-500">
                  太棒了！
                </p>
                <p class="text-sm">
                  暂无错题，继续保持！
                </p>
              </div>

              <div
                v-else
                class="space-y-3"
              >
                <div
                  v-for="question in wrongQuestions"
                  :key="question.id"
                  class="bg-gray-50 rounded-xl p-4 hover:bg-gray-100 transition-colors cursor-pointer group border border-gray-200 hover:border-red-200"
                  @click="startPractice(question)"
                >
                  <div class="flex items-start justify-between mb-2">
                    <span
                      :class="[
                        'px-2 py-0.5 text-xs font-medium rounded-full',
                        getTypeColor(question.type),
                      ]"
                    >
                      {{ getTypeName(question.type) }}
                    </span>
                    <span class="text-xs text-gray-400">
                      {{ question.lastPracticeTime }}
                    </span>
                  </div>
                  <h4
                    class="text-sm font-medium text-gray-800 mb-2 line-clamp-2 group-hover:text-red-600 transition-colors"
                  >
                    {{ question.title }}
                  </h4>
                  <div
                    class="flex items-center justify-between text-xs text-gray-500"
                  >
                    <span>难度: {{ getDifficultyLabel(question.difficulty) }}</span>
                    <span class="text-red-500 font-medium flex items-center">
                      <i class="fas fa-redo-alt mr-1" />重新练习
                    </span>
                  </div>
                </div>
              </div>
            </div>

            <!-- 底部操作 -->
            <div
              v-if="wrongQuestions.length > 0"
              class="border-t border-gray-200 p-4 bg-gray-50"
            >
              <button
                class="w-full py-3 bg-gradient-to-r from-red-500 to-orange-500 text-white rounded-xl font-medium hover:from-red-600 hover:to-orange-600 transition-all shadow-md hover:shadow-lg flex items-center justify-center"
                @click="practiceAllWrong"
              >
                <i class="fas fa-play-circle mr-2" />
                练习全部错题
              </button>
            </div>
          </div>
        </div>
      </transition>
    </teleport>

    <!-- 底部导航 -->
    <EndBar />
  </div>
</template>

<script setup>
import { ref, computed, onUnmounted } from "vue";
import { useRouter } from "vue-router";
import NavBar from "@/components/common/NavBar.vue";
import EndBar from "@/components/common/EndBar.vue";

const router = useRouter();

// 导航项
const navItems = [
  { label: "首页", onClick: () => gotoHome() },
  { label: "课程", onClick: () => gotoCourse() },
  { label: "题库", isActive: true },
  { label: "时间表", onClick: () => gotoTimeTable() },
  { label: "单词打卡", onClick: () => gotoWordCheckIn() },
  { label: "AI伴学", onClick: () => gotoAiChat() },
];

// 路由跳转方法
function gotoHome() {
  router.push({ name: "Home" }).catch(() => {});
}

function gotoCourse() {
  router.push({ name: "Course" }).catch(() => {});
}

function gotoTimeTable() {
  router.push({ name: "TimeTable" }).catch(() => {});
}

function gotoWordCheckIn() {
  router.push({ name: "WordCheckIn" }).catch(() => {});
}

function gotoAiChat() {
  router.push({ name: "AiChat" }).catch(() => {});
}

function gotoSettings() {
  router.push({ name: "Settings" }).catch(() => {});
}

// 搜索
const searchQuery = ref("");
const sortBy = ref("default");
const currentPage = ref(1);
const pageSize = 10;

// 筛选条件
const filters = ref({
  relatedToCourse: false,
  includeMyVocab: false,
  types: [],
  difficulty: [],
  status: [],
});

// 筛选选项
const questionTypes = [
  { label: "听力", value: "listening" },
  { label: "阅读", value: "reading" },
  { label: "写作", value: "writing" },
  { label: "语法", value: "grammar" },
  { label: "词汇", value: "vocabulary" },
];

const difficultyLevels = [
  { label: "初级", value: 1 },
  { label: "四六级", value: 2 },
  { label: "考研", value: 3 },
  { label: "托福雅思", value: 4 },
  { label: "专业", value: 5 },
];

const statusOptions = [
  { label: "未做", value: "new" },
  { label: "已对", value: "done" },
  { label: "做错", value: "wrong" },
  { label: "已收藏", value: "favorite" },
];

// 统计数据
const stats = ref({
  todayCount: 12,
  accuracy: 85,
  masteredWords: 1280,
});

// 模拟题目数据
const questions = ref([
  {
    id: 1,
    type: "reading",
    title: "日常生活英语阅读：我的家庭",
    preview:
      "My family is not very big. There are four people in my family: my parents, my sister and I...",
    content: `My family is not very big. There are four people in my family: my parents, my sister and I. My father is a teacher. He works at a local school. My mother is a nurse. She works at the city hospital.

My sister is a student. She is 15 years old and goes to middle school. I am 18 years old and I am a college student. I study English at university.

We live in a small but comfortable house. On weekends, we often have dinner together and watch TV. Sometimes we go to the park or visit our grandparents. I love my family very much.`,
    questions: [
      {
        question: "How many people are there in the family?",
        options: ["Three", "Four", "Five", "Six"],
        correctAnswer: 1,
        explanation: '文章开头明确说"There are four people in my family"。',
        relatedGrammar: "基础句型",
      },
      {
        question: "What does the father do?",
        options: ["A doctor", "A nurse", "A teacher", "A student"],
        correctAnswer: 2,
        explanation: '文中说"My father is a teacher"。',
      },
    ],
    difficulty: 1,
    relatedCourse: "基础英语",
    tags: ["日常生活", "家庭", "初级"],
    userStatus: "done",
    lastPracticeTime: "3天前",
    isFavorite: false,
    containsVocab: ["comfortable", "hospital"],
  },
  {
    id: 2,
    type: "grammar",
    title: "四六级语法：虚拟语气",
    preview:
      "If I had known about the meeting earlier, I _____ my schedule accordingly.",
    content: `虚拟语气是四六级考试中的重点语法项目。本练习帮助你掌握虚拟语气的各种用法。

虚拟语气主要分为三种类型：
1. 与现在事实相反的虚拟
2. 与过去事实相反的虚拟
3. 与将来事实可能相反的虚拟`,
    questions: [
      {
        question:
          "If I had known about the meeting earlier, I _____ my schedule accordingly.",
        options: [
          "would adjust",
          "would have adjusted",
          "will adjust",
          "had adjusted",
        ],
        correctAnswer: 1,
        explanation:
          '这是与过去事实相反的虚拟语气。从句用"had + 过去分词"，主句用"would have + 过去分词"。',
        relatedGrammar: "虚拟语气",
      },
      {
        question: "I wish I _____ harder when I was in college.",
        options: ["study", "studied", "had studied", "would study"],
        correctAnswer: 2,
        explanation:
          'wish后面表示与过去事实相反的愿望时，从句用过去完成时"had + 过去分词"。',
      },
    ],
    difficulty: 2,
    relatedCourse: "大学英语语法",
    tags: ["虚拟语气", "四六级", "语法"],
    userStatus: "wrong",
    lastPracticeTime: "2天前",
    isFavorite: true,
    containsVocab: [],
  },
  {
    id: 3,
    type: "reading",
    title: "考研英语阅读：科技与教育",
    preview:
      "Virtual reality (VR) technology has been making significant strides in recent years, transforming various industries...",
    content: `Virtual reality (VR) technology has been making significant strides in recent years, transforming various industries from gaming to healthcare. In education, VR offers unprecedented opportunities to create immersive learning experiences that were once impossible in traditional classroom settings.

One of the most compelling advantages of VR in education is its ability to transport students to different environments without leaving the classroom. History students can walk through ancient Rome, biology students can explore the human body from the inside, and astronomy students can travel through the solar system.

However, the implementation of VR in education is not without challenges. The cost of VR equipment remains a significant barrier for many schools, particularly those in underfunded districts.`,
    questions: [
      {
        question:
          "What is the main advantage of VR in education according to the passage?",
        options: [
          "It reduces the cost of education",
          "It allows students to experience environments they cannot physically visit",
          "It replaces the need for teachers",
          "It improves students' physical health",
        ],
        correctAnswer: 1,
        explanation:
          '根据文章第二段，VR最主要的优势是能让学生"transport to different environments"。',
        relatedGrammar: "定语从句",
      },
      {
        question:
          "What is one of the challenges mentioned about implementing VR in education?",
        options: [
          "Lack of educational content",
          "Student resistance to new technology",
          "High cost of VR equipment",
          "Shortage of VR-trained teachers",
        ],
        correctAnswer: 2,
        explanation:
          '文章第三段明确提到"The cost of VR equipment remains a significant barrier"。',
      },
    ],
    difficulty: 3,
    relatedCourse: "考研英语",
    tags: ["科技", "教育", "考研", "长难句"],
    userStatus: "new",
    lastPracticeTime: null,
    isFavorite: false,
    containsVocab: [
      "unprecedented",
      "immersive",
      "compelling",
      "implementation",
    ],
  },
  {
    id: 4,
    type: "listening",
    title: "托福听力：学术讲座",
    preview:
      "Listen to part of a lecture in an environmental science class about climate change...",
    content: `本听力材料模拟托福学术讲座场景。

讲座主题：气候变化对生态系统的影响

讲座讨论了全球变暖如何影响不同生态系统，包括极地、热带雨林和海洋生态系统。

[注：实际项目中这里会有音频播放器]`,
    questions: [
      {
        question:
          "According to the professor, which ecosystem is most vulnerable to climate change?",
        options: [
          "Tropical rainforests",
          "Polar regions",
          "Temperate forests",
          "Desert ecosystems",
        ],
        correctAnswer: 1,
        explanation: "根据讲座内容，极地地区对气候变化最为敏感。",
      },
      {
        question: "What does the professor imply about ocean acidification?",
        options: [
          "It is a minor concern",
          "It affects only fish populations",
          "It threatens coral reef ecosystems",
          "It has been solved by scientists",
        ],
        correctAnswer: 2,
        explanation: "教授暗示海洋酸化对珊瑚礁生态系统构成威胁。",
      },
    ],
    difficulty: 4,
    relatedCourse: "托福听力强化",
    tags: ["托福", "学术讲座", "环境科学"],
    userStatus: "new",
    lastPracticeTime: null,
    isFavorite: false,
    containsVocab: ["vulnerable", "acidification", "ecosystem"],
  },
  {
    id: 5,
    type: "vocabulary",
    title: "专业英语：医学术语",
    preview: "Medical terminology practice for healthcare professionals.",
    content: `本练习针对医学专业英语词汇，适合医学生和医护人员。

医学术语通常由词根、前缀和后缀组成。掌握这些构词规律，可以帮助你理解和记忆大量专业词汇。

常见医学前缀：
- cardio- (心脏)
- neuro- (神经)
- gastro- (胃)
- hemo-/hemat- (血液)`,
    questions: [
      {
        question: "The term 'cardiomyopathy' refers to a disease of the:",
        options: ["Brain", "Heart muscle", "Blood vessels", "Nervous system"],
        correctAnswer: 1,
        explanation:
          "cardio-表示心脏，myo-表示肌肉，-pathy表示疾病。所以cardiomyopathy是心肌病。",
      },
      {
        question: "'Hematology' is the study of:",
        options: [
          "The heart",
          "The brain",
          "Blood and blood diseases",
          "The liver",
        ],
        correctAnswer: 2,
        explanation: "hemat-表示血液，-ology表示学科。Hematology是血液学。",
      },
    ],
    difficulty: 5,
    relatedCourse: "医学英语",
    tags: ["医学", "专业术语", "词根"],
    userStatus: "new",
    lastPracticeTime: null,
    isFavorite: false,
    containsVocab: ["cardiomyopathy", "hematology", "neurology"],
  },
  {
    id: 6,
    type: "writing",
    title: "雅思写作Task2：科技话题",
    preview:
      "Some people believe that technology has made our lives more complicated. To what extent do you agree?",
    content: `雅思大作文题目：

"Some people believe that technology has made our lives more complicated rather than easier. To what extent do you agree or disagree?"

写作要求：
1. 字数：至少250词
2. 结构：引言-正文段落-结论
3. 观点要清晰，论证要充分
4. 使用多样化的词汇和句式

评分标准：任务完成度、连贯性、词汇、语法`,
    questions: [
      {
        question: "这是雅思写作的哪种题型？",
        options: [
          "Report类型",
          "Discussion类型",
          "Agree/Disagree类型",
          "Problem/Solution类型",
        ],
        correctAnswer: 2,
        explanation:
          "题目问'To what extent do you agree or disagree'，这是典型的同意/不同意类型题目。",
      },
    ],
    difficulty: 4,
    relatedCourse: "雅思写作技巧",
    tags: ["雅思", "写作", "议论文"],
    userStatus: "wrong",
    lastPracticeTime: "5天前",
    isFavorite: true,
    containsVocab: [],
  },
  {
    id: 7,
    type: "grammar",
    title: "初级语法：一般现在时",
    preview: "Learn how to use the simple present tense correctly.",
    content: `一般现在时是最基本的英语时态之一。

用法：
1. 表示经常性或习惯性的动作
2. 表示客观事实或普遍真理
3. 表示现在的状态

注意：第三人称单数要加-s或-es。`,
    questions: [
      {
        question: "She _____ to school every day.",
        options: ["go", "goes", "going", "went"],
        correctAnswer: 1,
        explanation: "主语是第三人称单数she，所以动词go要变成goes。",
      },
      {
        question: "The sun _____ in the east.",
        options: ["rise", "rises", "rose", "rising"],
        correctAnswer: 1,
        explanation:
          "这是表示客观事实的句子，用一般现在时，主语是第三人称单数。",
      },
    ],
    difficulty: 1,
    relatedCourse: "基础英语语法",
    tags: ["语法", "时态", "初级"],
    userStatus: "done",
    lastPracticeTime: "1周前",
    isFavorite: false,
    containsVocab: [],
  },
  {
    id: 8,
    type: "vocabulary",
    title: "四六级高频词汇",
    preview: "Master the most frequently tested vocabulary in CET-4/6.",
    content: `本练习精选四六级高频词汇，帮助你在语境中理解和记忆单词。

注意观察每个单词在句子中的用法，关注其词性、搭配和语义。`,
    questions: [
      {
        question:
          "The government has _____ a new policy to promote renewable energy.",
        options: ["implemented", "implicated", "implied", "imported"],
        correctAnswer: 0,
        explanation: 'implement意为"实施、执行"，常与policy、plan等词搭配。',
      },
      {
        question:
          "Despite the _____ evidence against him, the defendant insisted on his innocence.",
        options: ["overwhelming", "underwhelming", "overlapping", "overcoming"],
        correctAnswer: 0,
        explanation: 'overwhelming evidence意为"压倒性的证据"，是固定搭配。',
      },
    ],
    difficulty: 2,
    relatedCourse: "大学英语词汇",
    tags: ["四六级", "词汇", "高频"],
    userStatus: "new",
    lastPracticeTime: null,
    isFavorite: false,
    containsVocab: ["implemented", "overwhelming"],
  },
]);

// 用户的生词本（模拟）
const userVocab = ref([
  "unprecedented",
  "immersive",
  "compelling",
  "overwhelming",
]);

// 计算筛选后的题目
const filteredQuestions = computed(() => {
  let result = [...questions.value];

  // 搜索过滤
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase();
    result = result.filter(
      (q) =>
        q.title.toLowerCase().includes(query) ||
        q.preview.toLowerCase().includes(query) ||
        (Array.isArray(q.tags)
          ? q.tags.some((tag) => tag.toLowerCase().includes(query))
          : false)
    );
  }

  // 题型过滤
  if (filters.value.types.length > 0) {
    result = result.filter((q) => filters.value.types.includes(q.type));
  }

  // 难度过滤
  if (filters.value.difficulty.length > 0) {
    result = result.filter((q) =>
      filters.value.difficulty.includes(q.difficulty)
    );
  }

  // 状态过滤
  if (filters.value.status.length > 0) {
    result = result.filter((q) => {
      if (filters.value.status.includes("new") && q.userStatus === "new")
        return true;
      if (filters.value.status.includes("done") && q.userStatus === "done")
        return true;
      if (filters.value.status.includes("wrong") && q.userStatus === "wrong")
        return true;
      if (filters.value.status.includes("favorite") && q.isFavorite)
        return true;
      return false;
    });
  }

  // 包含生词过滤
  if (filters.value.includeMyVocab) {
    result = result.filter((q) =>
      Array.isArray(q.containsVocab)
        ? q.containsVocab.some((word) => userVocab.value.includes(word))
        : false
    );
  }

  // 排序
  if (sortBy.value === "difficulty-asc") {
    result.sort((a, b) => a.difficulty - b.difficulty);
  } else if (sortBy.value === "difficulty-desc") {
    result.sort((a, b) => b.difficulty - a.difficulty);
  } else if (sortBy.value === "latest") {
    result.sort((a, b) => b.id - a.id);
  }

  return result;
});

// 分页
const totalPages = computed(() =>
  Math.ceil(filteredQuestions.value.length / pageSize)
);
const paginatedQuestions = computed(() => {
  const start = (currentPage.value - 1) * pageSize;
  return filteredQuestions.value.slice(start, start + pageSize);
});

// 活跃筛选条件数量
const activeFilterCount = computed(() => {
  let count = 0;
  if (filters.value.relatedToCourse) count++;
  if (filters.value.includeMyVocab) count++;
  count += filters.value.types.length;
  count += filters.value.difficulty.length;
  count += filters.value.status.length;
  // 已移除 tags 筛选，不再计入
  return count;
});

// 重置筛选
function resetFilters() {
  filters.value = {
    relatedToCourse: false,
    includeMyVocab: false,
    types: [],
    difficulty: [],
    status: [],
  };
  searchQuery.value = "";
  sortBy.value = "default";
  currentPage.value = 1;
}

// 防抖搜索
let searchTimeout;
function debouncedSearch() {
  clearTimeout(searchTimeout);
  searchTimeout = setTimeout(() => {
    currentPage.value = 1;
  }, 300);
}

// 获取题型标签样式
function getTypeClass(type) {
  const classes = {
    listening: "bg-purple-100 text-purple-800",
    reading: "bg-blue-100 text-blue-800",
    writing: "bg-green-100 text-green-800",
    grammar: "bg-yellow-100 text-yellow-800",
    vocabulary: "bg-pink-100 text-pink-800",
  };
  return classes[type] || "bg-gray-100 text-gray-800";
}

// 获取题型标签文本
function getTypeLabel(type) {
  const labels = {
    listening: "听力",
    reading: "阅读",
    writing: "写作",
    grammar: "语法",
    vocabulary: "词汇",
  };
  return labels[type] || type;
}

// 获取难度标签样式
function getDifficultyClass(level) {
  const classes = {
    1: "bg-green-100 text-green-800",
    2: "bg-blue-100 text-blue-800",
    3: "bg-yellow-100 text-yellow-800",
    4: "bg-orange-100 text-orange-800",
    5: "bg-red-100 text-red-800",
  };
  return classes[level] || "bg-gray-100 text-gray-800";
}

// 获取难度标签文本
function getDifficultyLabel(level) {
  const labels = {
    1: "初级",
    2: "四六级",
    3: "考研",
    4: "托福雅思",
    5: "专业",
  };
  return labels[level] || "未知";
}

// 错题本中使用的类型颜色
function getTypeColor(type) {
  const colors = {
    listening: "bg-purple-100 text-purple-700",
    reading: "bg-blue-100 text-blue-700",
    writing: "bg-green-100 text-green-700",
    grammar: "bg-yellow-100 text-yellow-700",
    vocabulary: "bg-pink-100 text-pink-700",
  };
  return colors[type] || "bg-gray-100 text-gray-700";
}

// 错题本中使用的类型名称
function getTypeName(type) {
  const names = {
    listening: "听力",
    reading: "阅读",
    writing: "写作",
    grammar: "语法",
    vocabulary: "词汇",
  };
  return names[type] || type;
}

// 高亮生词
function highlightVocab(text) {
  if (!text) return "";
  let result = text;
  userVocab.value.forEach((word) => {
    const regex = new RegExp(`(${word})`, "gi");
    result = result.replace(
      regex,
      '<span class="bg-yellow-100 text-yellow-800 px-1 rounded">🔍 $1</span>'
    );
  });
  return result;
}

// 练习模态框
const showPracticeModal = ref(false);
const currentQuestion = ref(null);
const userAnswers = ref([]);
const showAnswer = ref(false);
const practiceTime = ref(0);
let practiceTimer = null;

function startPractice(question) {
  currentQuestion.value = question;
  userAnswers.value = new Array(question.questions.length).fill(null);
  showAnswer.value = false;
  practiceTime.value = 0;
  showPracticeModal.value = true;

  // 开始计时
  practiceTimer = setInterval(() => {
    practiceTime.value++;
  }, 1000);
}

function closePracticeModal() {
  showPracticeModal.value = false;
  currentQuestion.value = null;
  if (practiceTimer) {
    clearInterval(practiceTimer);
    practiceTimer = null;
  }
}

function getUserAnswer(index) {
  return userAnswers.value[index];
}

const allAnswered = computed(() => {
  return userAnswers.value.every((answer) => answer !== null);
});

function submitAnswers() {
  showAnswer.value = true;
  if (practiceTimer) {
    clearInterval(practiceTimer);
    practiceTimer = null;
  }

  // 计算正确数
  const correctCount = currentQuestion.value.questions.reduce(
    (count, item, index) => {
      return count + (userAnswers.value[index] === item.correctAnswer ? 1 : 0);
    },
    0
  );

  // 更新题目状态
  const question = questions.value.find(
    (q) => q.id === currentQuestion.value.id
  );
  if (question) {
    question.userStatus =
      correctCount === currentQuestion.value.questions.length
        ? "done"
        : "wrong";
    question.lastPracticeTime = "刚刚";
  }
}

function formatTime(seconds) {
  const mins = Math.floor(seconds / 60);
  const secs = seconds % 60;
  return `${mins.toString().padStart(2, "0")}:${secs.toString().padStart(2, "0")}`;
}

// 划词翻译
const showWordPopup = ref(false);
const selectedWord = ref("");
const wordTranslation = ref("");
const wordPopupPosition = ref({ x: 0, y: 0 });

function handleTextSelection() {
  const selection = window.getSelection();
  const text = selection.toString().trim();

  if (text && text.split(" ").length === 1) {
    selectedWord.value = text;
    wordTranslation.value = getWordTranslation(text);

    const range = selection.getRangeAt(0);
    const rect = range.getBoundingClientRect();
    wordPopupPosition.value = {
      x: rect.left,
      y: rect.bottom + 10,
    };
    showWordPopup.value = true;
  }
}

function getWordTranslation(word) {
  // 模拟词典查询
  const dict = {
    virtual: "adj. 虚拟的；实际上的",
    reality: "n. 现实；实际",
    unprecedented: "adj. 前所未有的；空前的",
    immersive: "adj. 沉浸式的；身临其境的",
    compelling: "adj. 引人注目的；令人信服的",
    implementation: "n. 实施；执行",
    overwhelming: "adj. 压倒性的；势不可挡的",
  };
  return dict[word.toLowerCase()] || "暂无释义";
}

function addToVocab() {
  if (!userVocab.value.includes(selectedWord.value.toLowerCase())) {
    userVocab.value.push(selectedWord.value.toLowerCase());
    alert(`已将"${selectedWord.value}"加入单词打卡`);
  }
  showWordPopup.value = false;
}

// 收藏
function toggleFavorite(question) {
  question.isFavorite = !question.isFavorite;
}

// 加入计划
const showAddPlanModal = ref(false);
const planDate = ref("");
const planNote = ref("");
const planQuestion = ref(null);

function addToPlan(question) {
  planQuestion.value = question;
  planDate.value = new Date().toISOString().split("T")[0];
  planNote.value = "";
  showAddPlanModal.value = true;
}

function confirmAddToPlan() {
  // TODO: 调用API将题目加入计划
  alert(`已将题目加入 ${planDate.value} 的学习计划`);
  showAddPlanModal.value = false;
}

// 移动端筛选
const showMobileFilter = ref(false);

// 错题本
const showErrorBook = ref(false);

// 获取所有错题
const wrongQuestions = computed(() => {
  return questions.value.filter((q) => q.userStatus === "wrong");
});

// 切换错题本显示
function toggleErrorBook() {
  showErrorBook.value = !showErrorBook.value;
}

// 练习全部错题
function practiceAllWrong() {
  if (wrongQuestions.value.length > 0) {
    showErrorBook.value = false;
    startPractice(wrongQuestions.value[0]);
  }
}

// 学习建议弹窗
const showSuggestionsModal = ref(false);

// 清理
onUnmounted(() => {
  if (practiceTimer) {
    clearInterval(practiceTimer);
  }
  if (searchTimeout) {
    clearTimeout(searchTimeout);
  }
});
</script>

<style scoped>
@keyframes fadeIn {
  from {
    opacity: 0;
    transform: scale(0.95);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

.animate-fadeIn {
  animation: fadeIn 0.3s ease-out;
}

/* 自定义滚动条 */
::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 4px;
}

::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 4px;
}

::-webkit-scrollbar-thumb:hover {
  background: #a1a1a1;
}

/* 文本截断 */
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* 错题本侧边栏滑入动画 */
.slide-right-enter-active,
.slide-right-leave-active {
  transition: all 0.3s ease;
}

.slide-right-enter-active > div:last-child,
.slide-right-leave-active > div:last-child {
  transition: transform 0.3s ease;
}

.slide-right-enter-from,
.slide-right-leave-to {
  opacity: 0;
}

.slide-right-enter-from > div:last-child,
.slide-right-leave-to > div:last-child {
  transform: translateX(100%);
}
</style>
