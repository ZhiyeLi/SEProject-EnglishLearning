<template>
  <el-dialog 
    v-model="isOpen" 
    title="修改密码" 
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
      <!-- 密码修改区域 -->
      <el-divider content-position="left">
        修改密码
      </el-divider>

      <!-- 原密码验证 -->
      <el-form-item 
        label="原密码"
        prop="oldPassword"
      >
        <el-input 
          v-model="form.oldPassword" 
          type="password"
          placeholder="请输入原密码"
        />
      </el-form-item>
      
      <!-- 新密码 -->
      <el-form-item
        label="新密码"
        prop="newPassword"
      >
        <el-input 
          v-model="form.newPassword" 
          type="password"
          placeholder="请输入新密码（6-20位）"
        />
      </el-form-item>
      
      <!-- 确认密码 -->
      <el-form-item
        label="确认密码"
        prop="confirmPassword"
      >
        <el-input 
          v-model="form.confirmPassword" 
          type="password"
          placeholder="请再次输入新密码"
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
        :loading="isLoading"
        :disabled="isLoading"
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
import { useRouter } from 'vue-router';
import { authApi } from '@/api';

// 注入全局用户状态
const userStore = useUserStore();
// 导入路由
const router = useRouter();
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
const form = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
}); // 表单数据

const isOpen = ref(false); // 对话框显示状态
const isLoading = ref(false); // 提交加载状态

// 表单校验规则
const rules = ref({
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur'},
    { min: 6, max: 20, message: '密码长度在6-20之间', trigger: 'blur'}
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur'},
    { min: 6, max: 20, message: '密码长度在6-20之间', trigger: 'blur'},
    {
      validator: (rule, value, callback) => {
        if (value === form.value.oldPassword) {
          callback(new Error('新密码不能与原密码相同'));
        } else {
          callback();
        }
      },
      trigger: 'blur'
    }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur'},
    { 
      validator: (rule, value, callback) => {
        if (value !== form.value.newPassword) {
          callback(new Error('两次输入的密码不一致'));
        } else {
          callback();
        }
      },
      trigger: 'blur'
    }
  ]
});

// 重置密码表单
const resetPasswordForm = () => {
  form.value.oldPassword = '';
  form.value.newPassword = '';
  form.value.confirmPassword = '';
  formRef.value?.clearValidate();
};

// 监听父组件的 open props，同步对话框状态
watch(() => props.open, (newVal) => {
  isOpen.value = newVal;
  if (newVal) {
    resetPasswordForm();
  }
}, { immediate: true });

// 监听对话框关闭，向父组件发送关闭事件
watch(isOpen, (newVal) => {
  emit('update:open', newVal);
});

// 提交表单
const submitForm = async () => {
  try {
    // 表单校验
    await formRef.value.validate();
    
    isLoading.value = true;
    
    // 调用后端API修改密码
    // 注意：request 拦截器已处理响应，成功返回 ApiResponse 对象
    const response = await authApi.changePassword({
      oldPassword: form.value.oldPassword,
      newPassword: form.value.newPassword
    });

    // 响应拦截器已处理，这里获得的就是 ApiResponse 对象
    ElMessage.success(response.message || '密码修改成功，请重新登录');
    
    // 清除用户登录状态
    userStore.logout();
    
    // 重置表单
    resetPasswordForm();
    
    // 关闭对话框
    isOpen.value = false;
    
    // 跳转到登录页
    router.push('/login');
  } catch (error) {
    // 如果执行到这里，说明响应拦截器已经捕获了错误
    // 不需要再手动处理错误消息，拦截器已显示
    console.error('修改密码失败：', error);
  } finally {
    isLoading.value = false;
  }
};
</script>

<style scoped>
</style>