<template>
  <teleport to="body">
    <div
      v-if="visible"
      class="fixed inset-0 bg-black bg-opacity-60 flex items-center justify-center z-50"
      @click.self="handleClose"
    >
      <div class="bg-white rounded-2xl shadow-2xl w-full max-w-4xl mx-4 overflow-hidden">
        <!-- 播放器 -->
        <div class="relative w-full" style="padding-top: 56.25%">
          <iframe
            v-if="bvid"
            :src="`https://player.bilibili.com/player.html?bvid=${bvid}&page=1`"
            class="absolute inset-0 w-full h-full border-0"
            allowfullscreen
            scrolling="no"
          />
          <div v-else class="absolute inset-0 flex items-center justify-center bg-gray-900 text-white">
            无法加载视频
          </div>
        </div>

        <!-- 底部操作栏 -->
        <div class="p-4 flex items-center justify-between border-t">
          <div class="flex items-center gap-3">
            <h3 class="text-lg font-semibold text-gray-800">{{ course.name }}</h3>
            <button
              class="text-lg transition-colors"
              :class="course.favorite ? 'text-yellow-500' : 'text-gray-300 hover:text-yellow-400'"
              @click="$emit('favorite', course.id)"
            >★</button>
          </div>

          <div class="flex items-center gap-3">
            <span v-if="course.status === 'completed'" class="text-green-600 text-sm">✓ 已学完</span>
            <button
              v-else
              class="px-4 py-2 bg-emerald-500 text-white rounded-lg text-sm hover:bg-emerald-600 transition-colors"
              @click="handleMarkComplete"
            >标记已学完</button>

            <button
              class="text-gray-400 hover:text-gray-600 transition-colors p-1"
              @click="handleClose"
            >✕</button>
          </div>
        </div>
      </div>
    </div>
  </teleport>
</template>

<script setup>
import { computed } from "vue";
import { ElMessageBox } from "element-plus";

const props = defineProps({
  course: { type: Object, required: true },
  visible: { type: Boolean, default: false },
});

const emit = defineEmits(["close", "complete", "favorite"]);

const bvid = computed(() => {
  const url = props.course.videoUrl || "";
  const match = url.match(/BV[a-zA-Z0-9]+/);
  return match ? match[0] : null;
});

function handleMarkComplete() {
  emit("complete", props.course.id);
}

function handleClose() {
  if (props.course.status !== "completed") {
    ElMessageBox.confirm("已学完了吗？", "提示", {
      confirmButtonText: "标记完成并关闭",
      cancelButtonText: "直接关闭",
      type: "info",
    }).then(() => {
      emit("complete", props.course.id);
      emit("close");
    }).catch(() => {
      emit("close");
    });
  } else {
    emit("close");
  }
}
</script>
