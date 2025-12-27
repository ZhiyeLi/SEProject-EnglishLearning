<template>
  <div class="min-h-screen bg-gray-50 flex flex-col">
    <!-- 引入NavBar组件，自定义右侧操作按钮 -->
    <NavBar :nav-items="navItems">
      <template #actions>
        <ActionButtons
          @suggestions="showSuggestionsModal = true"
          @settings="gotoSettings"
          @home="gotoHome"
          @notifications="handleNotifications"
        />
      </template>
    </NavBar>

    <!-- 主内容区 -->
    <main class="flex-grow flex flex-col md:flex-row">
      <!-- 左侧好友列表：保留 -->
      <aside class="w-full md:w-96 bg-white border-r border-gray-200 shadow-sm md:h-[calc(100vh-64px)] sticky top-[64px] overflow-y-auto flex-shrink-0 z-20">
        <div class="p-5 h-full flex flex-col">
          <!-- 好友列表区域 -->
          <div class="flex-grow overflow-y-auto -mx-2 px-2">
            <h3 class="text-sm font-semibold text-gray-500 uppercase tracking-wider mb-4 mt-2">
              好友列表
            </h3>
            
            <ul class="space-y-2">
              <!-- 加载状态 -->
              <li
                v-if="loading"
                class="p-3 text-center text-gray-500"
              >
                <i class="fas fa-spinner fa-spin mr-2" />加载中...
              </li>
              <!-- 错误提示 -->
              <li
                v-else-if="error"
                class="p-3 text-center text-red-500"
              >
                {{ error }}
              </li>
              <!-- 无好友提示 -->
              <li
                v-else-if="friendList.length === 0"
                class="p-3 text-center text-gray-500"
              >
                暂无好友，快去添加吧！
              </li>
              <!-- 遍历真实好友列表 -->
              <li
                v-for="friend in friendList"
                :key="friend.id"
              >
                <FriendItem 
                  :name="friend.name" 
                  :avatar="friend.avatar" 
                  :status="friend.status"
                >
                  <!-- 未读信息小红点 -->
                  <span
                    v-if="unreadCounts[friend.id] > 0"
                    class="absolute top-1/2 right-2 transform -translate-y-1/2 bg-red-500 text-white text-xs rounded-full h-5 w-5 flex items-center justify-center animate-pulse z-10"
                    :class="{ 'h-6 w-6 px-0.5': unreadCounts[friend.id] > 9 }"    
                  >
                    {{ unreadCounts[friend.id] > 99 ? '99+' : unreadCounts[friend.id] }}
                  </span>
                </FriendItem>
              </li>
              <li>
                <button 
                  class="w-full flex items-center justify-center p-3 text-emerald-600 text-sm border border-dashed border-emerald-200 rounded-lg hover:bg-emerald-50 transition-all hover:border-emerald-300 group"
                  @click="showAddFriendModal = true"
                >
                  <i class="fas fa-plus-circle mr-2 group-hover:scale-110 transition-transform" />
                  点击添加更多好友
                </button>
              </li>

              <!-- 好友请求按钮（放在添加好友按钮下方） -->
              <li>
                <button 
                  class="w-full flex items-center justify-center p-3 text-emerald-600 text-sm border border-dashed border-emerald-200 rounded-lg hover:bg-emerald-50 transition-all hover:border-emerald-300 group relative"
                  @click="showFriendRequestModal = true"
                >
                  <i class="fas fa-user-plus mr-2 group-hover:scale-110 transition-transform" />
                  查看好友请求
                  <!-- 未处理请求数小红点 -->
                  <span
                    v-if="friendRequests.length > 0"
                    class="absolute top-1 right-6 bg-red-500 text-white text-xs rounded-full h-4 w-4 flex items-center justify-center animate-pulse"
                  >
                    {{ friendRequests.length }}
                  </span>
                </button>
              </li>
            </ul>
          </div>
          
          <!-- 底部功能选项：添加了点击事件 -->
          <div class="border-t border-gray-100 mt-4 pt-3 flex justify-around">
            <button
              :class="[
                'flex flex-col items-center py-1 transition-colors',
                activeTab === 'friends' ? 'text-emerald-600 hover:text-emerald-700' : 'text-gray-600 hover:text-emerald-600'
              ]"
              @click="gotoHome"
            >
              <i class="fas fa-home text-xl mb-1" />
              <span class="text-sm">首页</span>
            </button>

            <button
              :class="[
                'flex flex-col items-center py-1 transition-colors',
                activeTab === 'chat' ? 'text-emerald-600 hover:text-emerald-700' : 'text-gray-600 hover:text-emerald-600'
              ]"
              @click="gotoChat"
            >
              <i class="fas fa-comment text-xl mb-1" />
              <span class="text-sm">聊天</span>
            </button>

            <button
              :class="[
                'flex flex-col items-center py-1 transition-colors',
                activeTab === 'rank' ? 'text-emerald-600 hover:text-emerald-700' : 'text-gray-600 hover:text-emerald-600'
              ]"
              @click="gotoRank"
            >
              <i class="fas fa-trophy text-xl mb-1" />
              <span class="text-sm">排行榜</span>
            </button>
          </div>
        </div>
      </aside>

      <!-- 中间内容区：清空打卡数字和计划项目 -->
      <div class="flex-grow p-6 overflow-y-auto">
        <div class="max-w-5xl mx-auto space-y-6">
          <!-- 打卡统计卡片：清空具体数字，保留框架 -->
          <div
            class="bg-white rounded-xl shadow-sm border border-gray-200 p-5 relative overflow-hidden transform transition-all duration-300 hover:shadow-md"
          >
            <div
              class="absolute top-0 right-0 w-40 h-40 bg-gradient-to-br from-emerald-400 via-teal-400 to-cyan-400 rounded-full -mr-16 -mt-16 opacity-20"
            />

            <div
              class="flex flex-col md:flex-row justify-between items-start md:items-center"
            >
              <div>
                <h2
                  class="text-2xl font-bold text-emerald-600 mb-3 flex items-center"
                >
                  已连续打卡 -- 天！！！
                  <span class="ml-2 text-yellow-500 animate-pulse"><i class="fas fa-fire" /></span>
                </h2>
                <ul class="space-y-2 text-gray-700 text-base">
                  <li class="flex items-center">
                    <i
                      class="fas fa-plus-circle text-emerald-500 mr-2"
                    />今日新学 <span class="font-semibold">-- 个单词</span>
                  </li>
                  <li class="flex items-center">
                    <i class="fas fa-sync text-yellow-500 mr-2" />今日复习
                    <span class="font-semibold">-- 个单词</span>
                  </li>
                  <li class="flex items-center">
                    <i
                      class="fas fa-calendar-alt text-emerald-500 mr-2"
                    />明日需复习 <span class="font-semibold">-- 个单词</span>
                  </li>
                </ul>
              </div>

              <div class="flex items-center mt-6 md:mt-0">
                <div class="relative mr-6">
                  <div
                    class="w-28 h-28 bg-gradient-to-r from-emerald-300 via-teal-200 to-cyan-300 rounded-full flex items-center justify-center shadow-md"
                  >
                    <i class="fas fa-paw text-4xl text-white" />
                  </div>
                  <div
                    class="absolute -top-2 left-1/2 transform -translate-x-1/2 w-24 h-6 bg-gradient-to-r from-emerald-400 via-teal-300 to-cyan-400 rounded-full shadow-sm"
                  />
                </div>

                <button
                  class="bg-emerald-500 hover:bg-emerald-600 text-white px-6 py-3 rounded-lg font-medium transition-all shadow-md hover:shadow-lg transform hover:-translate-y-0.5 text-base"
                  @click="startWordCheckIn"
                >
                  开始背单词 <i class="fas fa-arrow-right ml-1" />
                </button>
              </div>
            </div>
          </div>

          <!-- 计划时间表：使用CustomButton -->
          <div
            class="bg-white rounded-xl shadow-sm border border-gray-200 p-5 transform transition-all duration-300 hover:shadow-md"
          >
            <h3
              class="text-lg font-semibold text-gray-800 mb-4 flex items-center"
            >
              <i
                class="fas fa-calendar-check text-emerald-500 mr-2"
              />计划时间表
            </h3>
            <p class="text-lg font-bold text-sky-500 mb-4">
              {{ currentDateStr }}
            </p>

            <div class="grid grid-cols-7 gap-2 text-center">
              <!-- 星期标题：保留 -->
              <div class="text-sm font-medium text-gray-500 py-2">
                一
              </div>
              <div class="text-sm font-medium text-gray-500 py-2">
                二
              </div>
              <div class="text-sm font-medium text-gray-500 py-2">
                三
              </div>
              <div class="text-sm font-medium text-gray-500 py-2">
                四
              </div>
              <div class="text-sm font-medium text-gray-500 py-2">
                五
              </div>
              <div class="text-sm font-medium text-gray-500 py-2">
                六
              </div>
              <div class="text-sm font-medium text-gray-500 py-2">
                日
              </div>

              <!-- 本周计划显示 -->
              <div
                v-for="(dayPlans, index) in weekPlans"
                :key="index"
                class="bg-gray-50 rounded-lg p-3 shadow-sm border border-gray-100 min-h-[80px]"
                :class="
                  getUncompletedPlans(dayPlans).length > 0
                    ? 'cursor-pointer hover:bg-emerald-50'
                    : ''
                "
                @click="
                  getUncompletedPlans(dayPlans).length > 0 && gotoTimeTable()
                "
              >
                <div
                  v-if="getUncompletedPlans(dayPlans).length === 0"
                  class="text-sm text-gray-400 flex items-center justify-center h-full"
                >
                  无计划
                </div>
                <div
                  v-else
                  class="space-y-1"
                >
                  <div
                    v-for="plan in getUncompletedPlans(dayPlans).slice(0, 2)"
                    :key="plan.id"
                    class="text-xs p-1 rounded truncate flex items-center"
                    :class="getPlanPriorityClass(plan.priority)"
                  >
                    <div
                      class="w-1.5 h-1.5 rounded-full mr-1 flex-shrink-0"
                      :class="getPlanDotClass(plan.priority)"
                    />
                    <span class="truncate">{{ plan.title }}</span>
                  </div>
                  <div
                    v-if="getUncompletedPlans(dayPlans).length > 2"
                    class="text-xs text-gray-500 text-center"
                  >
                    +{{ getUncompletedPlans(dayPlans).length - 2 }}
                  </div>
                </div>
              </div>
            </div>

            <!-- 新增按钮：使用CustomButton -->
            <CustomButton
              type="secondary"
              icon="fa-plus-circle"
              text="管理学习计划"
              class="mt-4 text-emerald-600 hover:text-emerald-700 text-base flex items-center transition-colors transform hover:-translate-x-0.5"
              @click="gotoTimeTable"
            />
          </div>

          <!-- 文章推送：保持原样（用户未要求修改） -->
          <div
            class="bg-white rounded-xl shadow-sm border border-gray-200 p-5 transform transition-all duration-300 hover:shadow-md"
          >
            <h3
              class="text-xl font-semibold text-gray-800 mb-5 flex items-center"
            >
              <i class="fas fa-newspaper text-emerald-500 mr-2" /> 推荐学习文章
            </h3>

            <div class="space-y-6">
              <!-- 文章1 -->
              <div
                class="group flex flex-col md:flex-row gap-5 pb-5 border-b border-gray-100 hover:bg-emerald-50 p-2 rounded-lg transition-all duration-200"
                @click="gotoArticleDetail('article1')"
              >
                <div
                  class="w-full md:w-56 h-40 flex-shrink-0 overflow-hidden rounded-lg shadow-sm"
                >
                  <img
                    src="https://picsum.photos/seed/english1/400/300"
                    alt="英语听力技巧"
                    class="w-full h-full object-cover transition-transform duration-500 group-hover:scale-110"
                  >
                </div>
                <div class="flex-grow">
                  <h4
                    class="font-bold text-gray-800 text-xl group-hover:text-emerald-600 transition-colors"
                  >
                    如何有效提高英语听力水平
                  </h4>
                  <p class="text-base text-gray-600 mt-2 line-clamp-2">
                    本文介绍了几种实用的英语听力训练方法，帮助学习者快速提升听力理解能力，包括精听与泛听结合、影子跟读法等技巧，适合各阶段英语学习者参考。
                  </p>
                  <div
                    class="mt-3 text-sm text-gray-500 flex items-center justify-between"
                  >
                    <div>
                      <span class="mr-4"><i class="far fa-eye mr-1" /> 2.3k 阅读</span>
                      <span><i class="far fa-comment mr-1" /> 56 评论</span>
                    </div>
                    <span class="text-emerald-500"><i class="far fa-clock mr-1" /> 5分钟阅读</span>
                  </div>
                </div>
              </div>

              <!-- 文章2 -->
              <div
                class="group flex flex-col md:flex-row gap-5 pb-5 border-b border-gray-100 hover:bg-emerald-50 p-2 rounded-lg transition-all duration-200"
                @click="gotoArticleDetail('article2')"              
              >
                <div
                  class="w-full md:w-56 h-40 flex-shrink-0 overflow-hidden rounded-lg shadow-sm"
                >
                  <img
                    src="https://picsum.photos/seed/english2/400/300"
                    alt="英语作文技巧"
                    class="w-full h-full object-cover transition-transform duration-500 group-hover:scale-110"
                  >
                </div>
                <div class="flex-grow">
                  <h4
                    class="font-bold text-gray-800 text-xl group-hover:text-emerald-600 transition-colors"
                  >
                    高考英语作文高分技巧
                  </h4>
                  <p class="text-base text-gray-600 mt-2 line-clamp-2">
                    掌握这些写作技巧，让你的英语作文在考试中脱颖而出，轻松获得高分。从结构布局到高级词汇运用，全面解析评分要点和得分技巧。
                  </p>
                  <div
                    class="mt-3 text-sm text-gray-500 flex items-center justify-between"
                  >
                    <div>
                      <span class="mr-4"><i class="far fa-eye mr-1" /> 3.1k 阅读</span>
                      <span><i class="far fa-comment mr-1" /> 89 评论</span>
                    </div>
                    <span class="text-emerald-500"><i class="far fa-clock mr-1" /> 7分钟阅读</span>
                  </div>
                </div>
              </div>

              <!-- 文章3 -->
              <div
                class="group flex flex-col md:flex-row gap-5 hover:bg-emerald-50 p-2 rounded-lg transition-all duration-200"
                @click="gotoArticleDetail('article3')"
              >
                <div
                  class="w-full md:w-56 h-40 flex-shrink-0 overflow-hidden rounded-lg shadow-sm"
                >
                  <img
                    src="https://picsum.photos/seed/english3/400/300"
                    alt="英语语法学习"
                    class="w-full h-full object-cover transition-transform duration-500 group-hover:scale-110"
                  >
                </div>
                <div class="flex-grow">
                  <h4
                    class="font-bold text-gray-800 text-xl group-hover:text-emerald-600 transition-colors"
                  >
                    30天掌握英语语法核心知识点
                  </h4>
                  <p class="text-base text-gray-600 mt-2 line-clamp-2">
                    系统化学习语法知识，30天计划帮助你构建完整的英语语法体系，从基础句型到复杂句式，循序渐进打好语法基础，适合中高考学生。
                  </p>
                  <div
                    class="mt-3 text-sm text-gray-500 flex items-center justify-between"
                  >
                    <div>
                      <span class="mr-4"><i class="far fa-eye mr-1" /> 1.8k 阅读</span>
                      <span><i class="far fa-comment mr-1" /> 42 评论</span>
                    </div>
                    <span class="text-emerald-500"><i class="far fa-clock mr-1" /> 6分钟阅读</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧边栏：保持原样（用户未要求修改） -->
      <aside class="w-full md:w-72 flex-shrink-0 p-6 hidden lg:block">
        <!-- 当前使用词典 -->
        <div
          class="bg-white rounded-xl shadow-sm border border-gray-200 p-4 mb-6 transform transition-all duration-300 hover:shadow-md"
        >
          <h3
            class="text-base font-semibold text-gray-800 mb-3 flex items-center"
          >
            <i class="fas fa-book text-emerald-500 mr-2" /> 当前使用词典
          </h3>

          <div
            class="bg-emerald-50 rounded-lg p-3 mb-3 border border-emerald-100"
          >
            <div class="font-medium text-gray-800 text-base">
              高考3500词
            </div>
            <div class="text-base text-gray-600 mt-2 space-y-1">
              <p class="flex justify-between">
                <span>已背单词</span>
                <span class="font-medium text-emerald-600">1,280 个</span>
              </p>
              <div class="w-full bg-gray-200 rounded-full h-1.5 mt-1">
                <div
                  class="bg-emerald-500 h-1.5 rounded-full"
                  style="width: 36%"
                />
              </div>
              <p class="flex justify-between mt-2">
                <span>剩余单词</span>
                <span class="font-medium text-gray-700">2,220 个</span>
              </p>
            </div>
          </div>

          <button
            class="w-full bg-emerald-50 hover:bg-emerald-100 text-emerald-700 py-2 rounded-lg transition-all transform hover:-translate-y-0.5 shadow-sm hover:shadow text-base"
          >
            更换词典 <i class="fas fa-exchange-alt ml-1" />
          </button>
        </div>

        <!-- AI助手：使用CustomButton -->
        <div
          class="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden transform transition-all duration-300 hover:shadow-md"
        >
          <div
            class="bg-gradient-to-r from-emerald-500 to-emerald-600 p-3 border-b border-gray-200"
          >
            <h3 class="text-base font-semibold text-white flex items-center">
              <i class="fas fa-robot mr-2" /> AI学习助手
            </h3>
          </div>

          <div class="p-4 flex flex-col items-center">
            <div
              class="w-20 h-20 bg-gradient-to-r from-emerald-100 to-emerald-200 rounded-full flex items-center justify-center mb-3 shadow-sm"
            >
              <i class="fas fa-robot text-emerald-600 text-3xl" />
            </div>

            <p class="text-base text-gray-600 text-center mb-4 px-2">
              有任何语法或作文问题，随时问我哦~
            </p>

            <CustomButton
              type="primary"
              icon="fa-comments"
              text="开始对话"
              class="w-full py-2.5 rounded-lg transition-all shadow hover:shadow-md transform hover:-translate-y-0.5 text-base"
              @click="gotoAiChat"
            />
          </div>
        </div>
      </aside>
    </main>

    <!-- 引入EndBar组件 -->
    <EndBar />

    <!-- 添加好友弹窗 -->
    <teleport to="body">
      <div
        v-if="showAddFriendModal"
        class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 animate-fadeIn"
      >
        <div class="bg-white rounded-lg shadow-xl w-full max-w-md mx-4 transform transition-all scale-100">
          <!-- 弹窗头部 -->
          <div class="px-6 py-4 border-b border-gray-200 flex justify-between items-center">
            <h3 class="text-lg font-semibold text-gray-800">
              添加好友
            </h3>
            <button 
              class="text-gray-400 hover:text-gray-600 transition-colors"
              @click="showAddFriendModal = false"
            >
              <i class="fas fa-times text-lg" />
            </button>
          </div>
          
          <!-- 弹窗内容 -->
          <div class="px-6 py-4">
            <div class="space-y-4">
              <!-- 搜索好友输入框 -->
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">搜索好友（用户名/ID/邮箱）</label>
                <div class="relative">
                  <input 
                    v-model="searchFriendValue"
                    type="text" 
                    placeholder="请输入好友信息..." 
                    class="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent transition-all text-base"
                  >
                  <i class="fas fa-search absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" />
                </div>
              </div>
              
              <!-- 搜索结果 -->
              <div
                v-if="searchFriendValue"
                class="max-h-40 overflow-y-auto border rounded-lg"
              >
                <div
                  v-if="searchResults.length === 0"
                  class="p-4 text-center text-gray-500"
                >
                  未找到相关好友
                </div>
                <div
                  v-else
                  class="divide-y"
                >
                  <div 
                    v-for="(friend, index) in searchResults" 
                    :key="index"
                    class="flex items-center p-3 hover:bg-gray-50 transition-colors"
                  >
                    <img 
                      :src="friend.avatar" 
                      alt="好友头像" 
                      class="w-10 h-10 rounded-full object-cover mr-3"
                    >
                    <div class="flex-grow">
                      <p class="font-medium text-gray-800">
                        {{ friend.name }}
                      </p>
                      <p class="text-xs text-gray-500">
                        ID: {{ friend.id }}
                      </p>
                    </div>
                    <button 
                      class="bg-emerald-500 hover:bg-emerald-600 text-white px-3 py-1 rounded-lg text-sm transition-colors"
                      @click="addFriend(friend)"
                    >
                      添加
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>
          
          <!-- 弹窗底部 -->
          <div class="px-6 py-3 border-t border-gray-200 flex justify-end space-x-2">
            <button 
              class="px-4 py-2 border border-gray-300 rounded-lg text-gray-700 hover:bg-gray-50 transition-colors"
              @click="showAddFriendModal = false"
            >
              取消
            </button>
            <button 
              class="px-4 py-2 bg-emerald-500 hover:bg-emerald-600 text-white rounded-lg transition-colors"
              :disabled="!friendId && !searchFriendValue"
              @click="confirmAddFriend"
            >
              确认添加
            </button>
          </div>
        </div>
      </div>
    </teleport>
    <!-- 好友请求确认弹窗 -->
    <teleport to="body">
      <div
        v-if="showFriendRequestModal"
        class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 animate-fadeIn"
      >
        <div class="bg-white rounded-lg shadow-xl w-full max-w-md mx-4 transform transition-all scale-100">
          <!-- 弹窗头部（原有） -->
          <div class="px-6 py-4 border-b border-gray-200 flex justify-between items-center">
            <h3 class="text-lg font-semibold text-gray-800">
              好友请求
            </h3>
            <button 
              class="text-gray-400 hover:text-gray-600 transition-colors"
              @click="showFriendRequestModal = false"
            >
              <i class="fas fa-times text-lg" />
            </button>
          </div>

          <!-- 弹窗内容（修改：添加加载状态） -->
          <div class="px-6 py-4 max-h-80 overflow-y-auto">
            <!-- 加载状态 -->
            <div
              v-if="friendRequestLoading"
              class="p-8 text-center text-gray-500"
            >
              <i class="fas fa-spinner fa-spin text-4xl mb-2 text-gray-300" />
              <p>加载中...</p>
            </div>
            <!-- 无请求 -->
            <div
              v-else-if="friendRequests.length === 0"
              class="p-8 text-center text-gray-500"
            >
              <i class="fas fa-inbox text-4xl mb-2 text-gray-300" />
              <p>暂无未处理的好友请求</p>
            </div>
            <!-- 有请求列表（原有：保留time显示） -->
            <div
              v-else
              class="space-y-3 divide-y"
            >
              <div 
                v-for="(request, index) in friendRequests" 
                :key="index"
                class="py-3 flex items-center justify-between"
              >
                <div class="flex items-center">
                  <img 
                    :src="request.avatar" 
                    alt="请求者头像" 
                    class="w-12 h-12 rounded-full object-cover mr-3"
                  >
                  <div>
                    <p class="font-medium text-gray-800">
                      {{ request.senderName }}
                    </p>
                    <p class="text-xs text-gray-500">
                      ID: {{ request.requesterId }} <!-- 显示发送者ID（替换原有request.id，因为request.id是请求的主键） -->
                    </p>
                    <p class="text-xs text-gray-400 mt-1">
                      {{ request.time }} <!-- 显示格式化后的时间 -->
                    </p>
                  </div>
                </div>
                <div class="flex space-x-2">
                  <button 
                    class="bg-green-500 hover:bg-green-600 text-white px-3 py-1 rounded-lg text-sm transition-colors"
                    @click="acceptFriendRequest(index)"
                  >
                    <i class="fas fa-check mr-1" /> 接受
                  </button>
                  <button 
                    class="bg-red-500 hover:bg-red-600 text-white px-3 py-1 rounded-lg text-sm transition-colors"
                    @click="rejectFriendRequest(index)"
                  >
                    <i class="fas fa-times mr-1" /> 拒绝
                  </button>
                </div>
              </div>
            </div>
          </div>
      
          <!-- 弹窗底部 -->
          <div class="px-6 py-3 border-t border-gray-200 flex justify-end">
            <button 
              class="px-4 py-2 bg-gray-100 hover:bg-gray-200 text-gray-700 rounded-lg transition-colors"
              @click="showFriendRequestModal = false"
            >
              关闭
            </button>
          </div>
        </div>
      </div>
    </teleport>

    <!-- 学习建议弹窗 -->
    <teleport to="body">
      <div
        v-if="showSuggestionsModal"
        class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 animate-fadeIn"
        @click="handleSuggestionsBackdropClick"
      >
        <div
          class="bg-white rounded-2xl shadow-2xl w-full max-w-2xl mx-4 max-h-[70vh] overflow-hidden transform transition-all"
          @click.stop
        >
          <!-- 弹窗头部 -->
          <div
            class="px-8 py-6 border-b border-gray-200 bg-gradient-to-r from-emerald-50 to-blue-50"
          >
            <div class="flex justify-between items-center">
              <h2 class="text-2xl font-bold text-gray-900 flex items-center">
                <i class="fas fa-lightbulb text-yellow-500 mr-3" />
                学习建议
              </h2>
              <button
                class="text-gray-400 hover:text-gray-600 transition-colors"
                @click="showSuggestionsModal = false"
              >
                <i class="fas fa-times text-2xl" />
              </button>
            </div>
          </div>

          <!-- 弹窗内容 -->
          <div
            class="px-8 py-6 overflow-y-auto"
            style="max-height: calc(70vh - 140px)"
          >
            <div class="space-y-4">
              <!-- 建议内容 -->
              <div>
                <h3 class="text-lg font-semibold text-gray-800 mb-3">
                  <span class="text-emerald-600">{{
                    suggestionsData[currentSuggestionIndex].title
                  }}</span>
                </h3>
                <p class="text-gray-700 leading-relaxed whitespace-pre-wrap">
                  {{ suggestionsData[currentSuggestionIndex].content }}
                </p>
              </div>

              <!-- 建议标签 -->
              <div class="flex flex-wrap gap-2 pt-4">
                <span
                  v-for="tag in suggestionsData[currentSuggestionIndex].tags"
                  :key="tag"
                  class="px-3 py-1 bg-emerald-50 text-emerald-700 rounded-full text-sm"
                >
                  {{ tag }}
                </span>
              </div>
            </div>
          </div>

          <!-- 弹窗底部 - 翻页控制 -->
          <div
            class="px-8 py-4 border-t border-gray-200 bg-gray-50 flex justify-between items-center"
          >
            <button
              :disabled="currentSuggestionIndex === 0"
              class="px-6 py-2 rounded-lg font-medium transition-all"
              :class="
                currentSuggestionIndex === 0
                  ? 'bg-gray-100 text-gray-400 cursor-not-allowed'
                  : 'bg-emerald-50 text-emerald-700 hover:bg-emerald-100'
              "
              @click="previousSuggestion"
            >
              <i class="fas fa-chevron-left mr-2" />上一条
            </button>

            <div class="text-gray-600 font-medium">
              {{ currentSuggestionIndex + 1 }} / {{ suggestionsData.length }}
            </div>

            <button
              :disabled="currentSuggestionIndex === suggestionsData.length - 1"
              class="px-6 py-2 rounded-lg font-medium transition-all"
              :class="
                currentSuggestionIndex === suggestionsData.length - 1
                  ? 'bg-gray-100 text-gray-400 cursor-not-allowed'
                  : 'bg-emerald-50 text-emerald-700 hover:bg-emerald-100'
              "
              @click="nextSuggestion"
            >
              下一条<i class="fas fa-chevron-right ml-2" />
            </button>
          </div>
        </div>
      </div>
    </teleport>
  </div>
</template>

<script setup>
import { onMounted, ref, computed, watch, onBeforeUnmount } from "vue";
import { useRouter } from "vue-router";
import { wordProgressManager } from "@/utils/wordData.js";
import { planManager } from "@/utils/planData.js";
import NavBar from "@/components/common/NavBar.vue";
import ActionButtons from "@/components/common/ActionButtons.vue";
import FriendItem from "@/components/business/FriendItem.vue";
import EndBar from "@/components/common/EndBar.vue";
import CustomButton from "@/components/common/CustomButton.vue";
import { friendApi } from '@/api/friend'; // 后端好友接口
// 引入文章数据方法
// eslint-disable-next-line no-unused-vars
import { articleList } from "@/data/articleData.js";

// 文章详情页跳转函数
const gotoArticleDetail = (articleId) => {
  router.push({ 
    name: "ArticleDetail", 
    params: { articleId: articleId } 
  }).catch(() => {});
};


const searchLoading = ref(false); // 搜索用户的加载状态

const loading = ref(false); // 加载状态
const error = ref(''); // 错误提示
const friendList = ref([]); // 后端返回的真实好友列表

// 添加好友弹窗相关响应式变量
const showAddFriendModal = ref(false);
const searchFriendValue = ref("");
const friendId = ref("");
const searchResults = ref([]);

// 好友请求弹窗相关响应式变量
const showFriendRequestModal = ref(false);

onMounted(async () => {
  await fetchFriendList(); // 加载好友列表
  await fetchFriendRequests(); // 加载好友请求列表
  await fetchUnreadCounts(); // 加载未读消息数

  // 开始轮询未读消息数
  if (!messagePollInterval.value) {
    messagePollInterval.value = setInterval(() => {
      fetchUnreadCounts().catch(err => console.warn('轮询未读数失败', err));
    }, 3000);
  }

  // 初始化单词打卡数据
  await wordProgressManager.init();

  // 初始化计划数据
  await planManager.initPlans();
  loadWeekPlans();

  // 页面滚动时导航栏样式变化（保持原样）
  const header = document.querySelector("header");
  window.addEventListener("scroll", () => {
    if (window.scrollY > 10) {
      header.classList.add("shadow");
    } else {
      header.classList.remove("shadow");
    }
  });
});

// 计算当前日期字符串
const currentDateStr = computed(() => {
  const now = new Date();
  const year = now.getFullYear();
  const month = now.getMonth() + 1;
  const day = now.getDate();
  return `${year}年${month}月${day}日`;
});

// 本周计划数据
const weekPlans = ref([[], [], [], [], [], [], []]);

// 加载本周计划
async function loadWeekPlans() {
  const today = new Date();
  today.setHours(0, 0, 0, 0);

  // 获取本周一的日期
  const dayOfWeek = today.getDay();
  const diff = dayOfWeek === 0 ? -6 : 1 - dayOfWeek;
  const monday = new Date(today);
  monday.setDate(today.getDate() + diff);

  // 获取本周的7天计划
  const newWeekPlans = [];
  for (let i = 0; i < 7; i++) {
    const date = new Date(monday);
    date.setDate(monday.getDate() + i);
    const dayPlans = await planManager.getPlansByDate(date);
    newWeekPlans.push(dayPlans);
  }

  weekPlans.value = newWeekPlans;
}

// 获取未完成的计划（按优先级排序）
function getUncompletedPlans(dayPlans) {
  const priorityOrder = { high: 1, medium: 2, low: 3 };
  return dayPlans
    .filter((plan) => !plan.completed)
    .sort((a, b) => priorityOrder[a.priority] - priorityOrder[b.priority]);
}

// 获取优先级背景颜色类
function getPlanPriorityClass(priority) {
  const colors = {
    high: "bg-red-50 text-red-700 border border-red-200",
    medium: "bg-yellow-50 text-yellow-700 border border-yellow-200",
    low: "bg-blue-50 text-blue-700 border border-blue-200",
  };
  return colors[priority] || "bg-gray-50 text-gray-700 border border-gray-200";
}

// 获取优先级圆点颜色类
function getPlanDotClass(priority) {
  const colors = {
    high: "bg-red-500",
    medium: "bg-yellow-500",
    low: "bg-blue-500",
  };
  return colors[priority] || "bg-gray-500";
}

// 底部 tab 狀態：預設選中好友
const activeTab = ref("friends");

const router = useRouter();

function gotoChat() {
  activeTab.value = "chat";
  // push to chat route
  router.push({ name: "Chat" }).catch(() => {});
}

function gotoHome() {
  activeTab.value = "friends";
  router.push({ name: "Home" }).catch(() => {});
}

function gotoAiChat() {
  router.push({ name: "AiChat" }).catch(() => {});
}

function gotoSettings() {
  router.push({ name: "Settings" }).catch(() => {});
}

function gotoTimeTable() {
  router.push({ name: "TimeTable" }).catch(() => {});
}
function gotoCourse() {
  router.push({ name: "Course" }).catch(() => {});
}
function gotoRank(){
  router.push({ name: "Rank" }).catch(() => {});
}
async function startWordCheckIn() {
  // 检查用户是否已选择过词汇类型
  const selectedType = await wordProgressManager.getSelectedType();

  if (selectedType) {
    // 如果已选择过，直接进入打卡页面
    router
      .push({
        name: "WordCheckIn",
        params: { typeId: selectedType.typeId },
      })
      .catch(() => {});
  } else {
    // 如果没选择过，先进入类型选择页面
    router.push({ name: "WordTypeSelection" }).catch(() => {});
  }
}

const navItems = [
  { label: "首页", onClick: gotoHome, isActive: true },
  { label: "课程", onClick: gotoCourse },
  {
    label: "题库",
    onClick: () => router.push({ name: "QuestionBank" }).catch(() => {}),
  },
  { label: "时间表", onClick: gotoTimeTable },
  { label: "单词打卡", onClick: startWordCheckIn },
  { label: "AI伴学", onClick: gotoAiChat },
];

//好友列表显示部分
//从后端获取好友list
const fetchFriendList = async () => {
  try {
    loading.value = true;
    const res = await friendApi.getFriendList(); // 调用后端接口

    if (res.code === 200) {
      friendList.value = res.data.map(friend => ({
        id: friend.userId, // 后端userId → 前端id
        name: friend.userName, // 后端userName → 前端name
        avatar: friend.avatar, // 头像字段直接复用
        status: friend.userStatus || 'offline', // 后端userStatus → 前端status，兜底默认值
        signature: friend.signature // 保留个性签名字段
      })); 
    } else {
      // 处理接口返回成功但code非200的情况
      error.value = res.message || '获取好友列表失败';
      console.error('获取好友列表失败：', res.message);
    } 
  } catch (err) {
    // 完善错误处理：区分网络错误、接口响应错误
    error.value = '网络异常，获取好友列表失败';
    console.error('获取好友列表异常：', err);
    if (err.response) {
      error.value = err.response.data.message || error.value;
      console.error('后端返回的错误信息：', err.response.data.message);
    }
  } finally {
    loading.value = false;
  }
};

//添加好友弹窗部分
let searchTimer = null; // 搜索防抖定时器
// 监听搜索关键词，调用后端接口搜索用户
watch(searchFriendValue, (val) => {
  // 1. 清空之前的定时器（防抖）
  if (searchTimer) clearTimeout(searchTimer);

  // 2. 如果输入为空，清空结果
  if (!val.trim()) {
    searchResults.value = [];
    return;
  }

  // 3. 开启新的定时器，延迟 500ms 执行搜索
  searchTimer = setTimeout(async () => {
    try {
      searchLoading.value = true;
      
      const res = await friendApi.searchFriend({ keyword: val });
      
      if (res.code === 200) {
        // 格式化后端返回的用户数据
        searchResults.value = res.data.map(user => ({
          id: user.userId, 
          name: user.userName, 
          avatar: user.avatar || 'https://picsum.photos/seed/default/100/100', 
          status: user.userStatus || 'offline'
        }));
      } else {
        searchResults.value = [];
      }
    } catch (err) {
      searchResults.value = [];
      console.error('搜索用户异常：', err);
    } finally {
      searchLoading.value = false;
    }
  }, 500); // 500ms 防抖时间
});


// 发送好友请求逻辑
const addFriend = async (friend) => {
  if (!friend || !friend.id) {
    alert("用户信息无效");
    return;
  }

  try {
    // 调用后端接口
    const res = await friendApi.sendFriendRequest({
      receiverId: friend.id // 传递目标用户ID
    });

    if (res.code === 200) {
      alert(res.message || '好友请求已发送');
      
      // 发送成功后关闭弹窗并清空搜索
      showAddFriendModal.value = false;
      searchFriendValue.value = '';
      searchResults.value = [];
      friendId.value = '';
    } else {
      // 处理业务错误（如：已经是好友、已发送过请求等）
      alert(res.message || '发送请求失败');
    }
  } catch (err) {
    console.error('发送好友请求异常:', err);
  }
};

// 确认添加按钮点击事件（保持逻辑一致，调用上面的addFriend）
const confirmAddFriend = () => {
  if (searchResults.value.length > 0) {
    // 如果有搜索结果，默认添加第一个
    addFriend(searchResults.value[0]);
  } else {
    alert("请先搜索并选择一个用户");
  }
};

// 好友请求弹窗部分
const friendRequestLoading = ref(false); // 好友请求加载状态
const friendRequests = ref([]); // 好友请求列表

// 新增：时间格式化工具函数（将后端的时间戳/日期字符串转为友好格式）
const formatTime = (timeStr) => {
  if (!timeStr) return '未知时间';
  const date = new Date(timeStr);
  // 格式：2025-12-12 14:30
  return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')} ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`;
};

// 从后端获取好友请求列表
const fetchFriendRequests = async () => {
  try {
    friendRequestLoading.value = true;
    const res = await friendApi.getFriendRequests(); // 调用新增的接口
    console.log('后端返回的好友请求原始数据：', res.data);
    if (res.code === 200) {
      // 格式化后端返回的请求数据（适配前端渲染）
      friendRequests.value = res.data.map(request => ({
        id: request.requestId, // 好友请求的唯一ID（后端主键）
        requesterId: request.senderId, // 发送请求的用户ID
        name: request.senderName, // 发送者昵称
        avatar: request.senderAvatar || 'https://picsum.photos/seed/default/100/100', // 发送者头像
        time: formatTime(request.createdAt), // 发送时间（格式化）
      }));
    } else {
      friendRequests.value = [];
      alert(res.message || '获取好友请求失败');
    }
  } catch (err) {
    friendRequests.value = [];
    console.error('获取好友请求异常:', err);
    const errMsg = err.response?.data?.message;
    alert(errMsg);
  } finally {
    friendRequestLoading.value = false;
  }
};

// 接受好友请求
const acceptFriendRequest = async (index) => {
  const request = friendRequests.value[index];
  if (!request || !request.id) {
    alert('请求信息无效');
    return;
  }

  try {
    // 调用接受请求的接口
    const res = await friendApi.acceptFriendRequest(request.id);
    if (res.code === 200) {
      alert(`已接受 ${request.name} 的好友请求！`);
      // 1. 从请求列表中移除该请求
      friendRequests.value.splice(index, 1);
      // 2. 刷新好友列表（可选：让新好友立即显示在列表中）
      await fetchFriendList();
    } else {
      alert(res.message || '接受好友请求失败');
    }
  } catch (err) {
    console.error('接受好友请求异常:', err);
    const errMsg = err.response?.data?.message || '网络异常，无法接受请求';
    alert(errMsg);
  }
};

// 拒绝好友请求
const rejectFriendRequest = async (index) => {
  const request = friendRequests.value[index];
  if (!request || !request.id) {
    alert('请求信息无效');
    return;
  }

  try {
    // 调用拒绝请求的接口
    const res = await friendApi.rejectFriendRequest(request.id);
    if (res.code === 200) {
      alert(`已拒绝 ${request.name} 的好友请求！`);
      // 从请求列表中移除该请求
      friendRequests.value.splice(index, 1);
    } else {
      alert(res.message || '拒绝好友请求失败');
    }
  } catch (err) {
    console.error('拒绝好友请求异常:', err);
    const errMsg = err.response?.data?.message || '网络异常，无法拒绝请求';
    alert(errMsg);
  }
};
// 未读计数存储（键：好友ID，值：未读数量）
const unreadCounts = ref({});
// 未读计数轮询定时器
const messagePollInterval = ref(null);

// 工具函数：校验未读计数数据是否有效
const validateUnreadCountData = (data) => {
  if (Array.isArray(data)) {
    return data.every(item => {
      return (item.friendId !== undefined && (typeof item.friendId === 'number' || typeof item.friendId === 'string')) 
          && (item.count !== undefined && Number.isInteger(item.count) && item.count >= 0);
    });
  } else if (typeof data === 'object' && data !== null) {
    return Object.entries(data).every(([friendId, count]) => {
      return (friendId && (typeof friendId === 'number' || typeof friendId === 'string'))
          && (typeof count === 'number' && Number.isInteger(count) && count >= 0);
    });
  }
  return false;
};

// 从后端获取所有好友的未读消息数
const fetchUnreadCounts = async () => {
  try {
    console.debug('[fetchUnreadCounts] 开始请求后端未读计数接口');
    const res = await friendApi.getUnreadCount();
    console.debug('[fetchUnreadCounts] 后端原始返回数据：', res);

    if (res.code === 200) {
      const isDataValid = validateUnreadCountData(res.data);
      if (!isDataValid) {
        console.error('[fetchUnreadCounts] 后端返回的未读计数数据格式无效：', res.data);
        unreadCounts.value = {};
        return;
      }

      let newUnreadCounts = {};
      if (Array.isArray(res.data)) {
        res.data.forEach(item => {
          if (item.friendId && typeof item.count === 'number') {
            // 转为字符串，匹配前端好友ID（避免类型不匹配）
            const friendIdStr = String(item.friendId);
            newUnreadCounts[friendIdStr] = item.count;
          } else {
            console.warn('[fetchUnreadCounts] 无效的未读计数项：', item);
          }
        });
      } else if (typeof res.data === 'object' && res.data !== null) {
        newUnreadCounts = res.data;
        // 类型兼容处理
        Object.entries(newUnreadCounts).forEach(([friendId, count]) => {
          const friendIdStr = String(friendId);
          if (typeof count !== 'number' || count < 0) {
            console.warn('[fetchUnreadCounts] 无效的未读计数：好友ID=', friendId, '，计数=', count);
            newUnreadCounts[friendIdStr] = 0;
          } else {
            newUnreadCounts[friendIdStr] = count;
          }
        });
      } else {
        console.warn('[fetchUnreadCounts] 后端返回的data格式不支持：', res.data);
        newUnreadCounts = {};
      }

      unreadCounts.value = newUnreadCounts;
      console.debug('[fetchUnreadCounts] 处理后的未读计数：', unreadCounts.value);
    } else {
      console.warn('[fetchUnreadCounts] 后端返回非200状态：', res.code, res.message);
      unreadCounts.value = {};
    }
  } catch (err) {
    console.error('获取未读消息数失败：', err);
    if (err.response) {
      console.error('请求地址：', err.config.url);
      console.error('响应状态：', err.response.status);
      console.error('响应数据：', err.response.data);
    }
    unreadCounts.value = {};
  }
};

// 清理未读计数轮询定时器
const clearMessagePoll = () => {
  if (messagePollInterval.value) {
    clearInterval(messagePollInterval.value);
    messagePollInterval.value = null;
  }
};

// 页面销毁时清理轮询
onBeforeUnmount(() => {
  clearMessagePoll();
});

// 学习建议弹窗部分
const showSuggestionsModal = ref(false);
const currentSuggestionIndex = ref(0);
const suggestionsData = ref([
  {
    title: "坚持打卡是关键",
    content:
      "根据你最近的学习数据，我发现你有几天没有坚持打卡。研究表明，每日坚持背单词比一次性背很多个词更能提高长期记忆效果。\n\n建议：\n• 每天固定时间打卡，形成习惯\n• 选择在精力最充沛的时候\n• 即使只有10分钟，也要坚持打卡\n\n相信你能做到！",
    tags: ["打卡习惯", "坚持", "记忆法"],
  },
  {
    title: "利用零碎时间高效学习",
    content:
      "你可以充分利用上下班、等车、休息间隙等零碎时间来复习单词。这些时间虽然不长，但积累起来效果显著。\n\n建议：\n• 使用移动设备随时复习\n• 利用碎片化时间做单词练习\n• 在高峰期巩固之前学过的词汇\n\n每天15-20分钟的有效学习胜过一次性的1小时被动学习。",
    tags: ["时间管理", "碎片化学习", "效率"],
  },
  {
    title: "制定合理的每日目标",
    content:
      "根据你的学习进度，建议适当调整每日学习单词数量。过多会导致疲劳，过少则影响进度。\n\n建议：\n• 四级备考阶段：每天50-100个单词\n• 六级备考阶段：每天80-120个单词\n• 根据个人吸收情况灵活调整\n\n记住：质量永远比数量重要！",
    tags: ["目标设置", "学习计划", "进度管理"],
  },
  {
    title: "重视拼写和发音",
    content:
      "单纯记忆单词的中文意思容易遗忘。建议同时关注单词的拼写、发音和用法。\n\n建议：\n• 大声朗读单词，加强发音记忆\n• 多做拼写练习，特别是容易混淆的词\n• 学习单词的衍生词和同义词\n\n这样学习的单词记忆时间会延长3倍以上。",
    tags: ["拼写", "发音", "词汇拓展"],
  },
  {
    title: "利用艾宾浩斯遗忘曲线",
    content:
      "我们的应用已经内置了艾宾浩斯遗忘曲线复习算法。系统会在最佳时间提醒你复习之前学过的单词。\n\n黄金复习时间点：\n• 第1次：学习后的1天\n• 第2次：学习后的3天\n• 第3次：学习后的7天\n• 第4次：学习后的15天\n• 第5次：学习后的30天\n\n按照系统提示复习，学习效果可提升5倍！",
    tags: ["遗忘曲线", "复习计划", "科学学习"],
  },
]);

// 下一条建议
const nextSuggestion = () => {
  if (currentSuggestionIndex.value < suggestionsData.value.length - 1) {
    currentSuggestionIndex.value++;
  }
};

// 上一条建议
const previousSuggestion = () => {
  if (currentSuggestionIndex.value > 0) {
    currentSuggestionIndex.value--;
  }
};

// 处理背景点击关闭弹窗
const handleSuggestionsBackdropClick = () => {
  showSuggestionsModal.value = false;
  currentSuggestionIndex.value = 0;
};
</script>

<style>
@import url("https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css");
@tailwind base;
@tailwind components;
@tailwind utilities;

/* 自定义滚动条样式（保持原样） */
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

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.animate-fadeIn {
  animation: fadeIn 0.5s ease-in-out;
}
</style>
