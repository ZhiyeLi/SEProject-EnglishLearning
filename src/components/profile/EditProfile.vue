<template>
  <el-dialog 
    v-model="isOpen" 
    title="编辑个人资料" 
    width="500px"
    :close-on-click-modal="false"
  >
    <el-form 
      ref="formRef" 
      :model="form" 
      :rules="rules"
      label-width="80px"
      class="mt-4"
    >
      <!-- 头像上传 -->
      <el-form-item label="头像">
        <div class="flex items-center gap-4">
          <!-- 预览头像 -->
          <el-avatar
            :size="80"
            class="border-2 border-gray-200"
          >
            <img
              :src="form.avatar"
              alt="预览头像"
              class="w-full h-full object-cover"
            >
          </el-avatar>
          <!-- 上传按钮 -->
          <el-upload
            class="upload-avatar"
            action="https://jsonplaceholder.typicode.com/posts/"  
            :file-list="fileList"
            :on-success="handleAvatarUpload"
            :before-upload="beforeAvatarUpload"
            accept="image/*"
            :auto-upload="true"
          >
            <!-- 替换为你的真实上传接口 -->
            <el-button
              type="primary"
              size="small"
            >
              更换头像
            </el-button>
          </el-upload>
        </div>
      </el-form-item>

      <!-- 昵称 -->
      <el-form-item
        label="昵称"
        prop="name"
      >
        <el-input 
          v-model="form.name" 
          placeholder="请输入昵称"
          max-length="16"
        />
      </el-form-item>

      <!-- 个性签名 -->
      <el-form-item
        label="个性签名"
        prop="signature"
      >
        <el-input 
          v-model="form.signature" 
          placeholder="请输入个性签名"
          type="textarea"
          :rows="3"
          max-length="50"
        />
      </el-form-item>

      <!-- 地区（可选） -->
      <el-form-item
        label="地区"
        prop="location"
      >
        <el-input 
          v-model="form.location" 
          placeholder="请输入地区"
          max-length="20"
        />
      </el-form-item>
    </el-form>

    <!-- 对话框底部按钮 -->
    <template #footer>
      <el-button @click="isOpen = false">
        取消
      </el-button>
      <el-button
        type="primary"
        @click="submitForm"
      >
        保存修改
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch } from 'vue';
import { ElMessage } from 'element-plus';
import { useUserStore } from '@/store/modules/user';

// 注入全局用户状态
const userStore = useUserStore();

// 接收父组件传递的“是否打开对话框”参数
const props = defineProps({
  open: {
    type: Boolean,
    default: false
  }
});

// 向父组件 emits 关闭事件
const emit = defineEmits(['update:open']);

// 表单相关
const formRef = ref(null); // 表单引用
const form = ref({}); // 表单数据
const fileList = ref([]); // 上传文件列表
const isOpen = ref(false); // 对话框显示状态

// 表单校验规则
const rules = ref({
  name: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { min: 2, max: 16, message: '昵称长度在 2-16 个字符之间', trigger: 'blur' }
  ],
  signature: [
    { max: 50, message: '个性签名不能超过 50 个字符', trigger: 'blur' }
  ],
  location: [
    { max: 20, message: '地区不能超过 20 个字符', trigger: 'blur' }
  ]
});

// 监听父组件的 open  props，同步对话框状态并回显数据
watch(() => props.open, (newVal) => {
  isOpen.value = newVal;
  if (newVal) {
    // 数据回显：将全局用户信息赋值给表单
    form.value = { ...userStore.userInfo };
  }
}, { immediate: true });

// 监听对话框关闭，向父组件发送关闭事件
watch(isOpen, (newVal) => {
  emit('update:open', newVal);
});

// 头像上传前校验（大小、格式）
const beforeAvatarUpload = (rawFile) => {
  const isImage = rawFile.type.startsWith('image/');
  if (!isImage) {
    ElMessage.error('请上传图片文件！');
    return false;
  }
  const isLt2M = rawFile.size / 1024 / 1024 < 2;
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB！');
    return false;
  }
  return true;
};

// 头像上传成功回调（替换为你的真实接口逻辑）
const handleAvatarUpload = (response, uploadFile) => {
  // 实际项目中，这里会接收后端返回的图片URL
  // 这里用临时链接模拟
  form.value.avatar = uploadFile.url || URL.createObjectURL(uploadFile.raw);
  ElMessage.success('头像上传成功！');
};

// 提交表单
const submitForm = async () => {
  try {
    // 表单校验
    await formRef.value.validate();
    // 调用Pinia的action更新用户信息
    userStore.updateUserInfo(form.value);
    // 关闭对话框
    isOpen.value = false;
    // 提示成功
    ElMessage.success('资料修改成功！');
  } catch (error) {
    // 校验失败或接口错误
    ElMessage.error('资料修改失败，请检查表单！');
    console.error('提交失败：', error);
  }
};
</script>

<style scoped>
.upload-avatar {
  margin-top: 10px;
}
.el-avatar {
  transition: transform 0.2s ease;
}
.el-avatar:hover {
  transform: scale(1.05);
}
</style>