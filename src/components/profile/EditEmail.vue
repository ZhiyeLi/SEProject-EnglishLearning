<template>
  <el-dialog 
    v-model="isOpen" 
    title="修改绑定邮箱" 
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
      <!-- 邮箱修改区域 -->
      <el-divider content-position="left">
        修改绑定邮箱
      </el-divider>

      <!-- 验证方式选择 -->
      <el-form-item label="验证方式">
        <el-radio-group
          v-model="verifyMethod"
          @change="resetEmailForm"
        >
          <el-radio label="password">
            输入原密码
          </el-radio>
          <el-radio label="email">
            原邮箱验证
          </el-radio>
          <el-radio label="phone">
            手机验证
          </el-radio>
        </el-radio-group>
      </el-form-item>
      
      <!-- 原密码验证 -->
      <el-form-item 
        v-if="verifyMethod === 'password'" 
        label="原密码"
        prop="oldPassword"
        :error="passwordError"
      >
        <el-input 
          v-model="form.oldPassword" 
          type="password"
          placeholder="请输入原密码"
          @blur="validateOldPassword"
        />
      </el-form-item>
      
      <!-- 原邮箱验证 -->
      <el-form-item 
        v-if="verifyMethod === 'email'" 
        label="验证码"
        prop="verifyCode"
      >
        <el-row :gutter="10">
          <el-col :span="14">
            <el-input 
              v-model="form.verifyCode" 
              placeholder="请输入原邮箱验证码"
            />
          </el-col>
          <el-col :span="10">
            <el-button 
              type="text" 
              :disabled="codeSending"
              @click="sendVerifyCode('email')"
            >
              {{ codeSending ? `${countDown}秒后重发` : `发送至${formatContact(userStore.userInfo.email)}` }}
            </el-button>
          </el-col>
        </el-row>
      </el-form-item>
      
      <!-- 手机验证 -->
      <el-form-item 
        v-if="verifyMethod === 'phone'" 
        label="验证码"
        prop="verifyCode"
      >
        <el-row :gutter="10">
          <el-col :span="14">
            <el-input 
              v-model="form.verifyCode" 
              placeholder="请输入手机验证码"
            />
          </el-col>
          <el-col :span="10">
            <el-button 
              type="text" 
              :disabled="codeSending"
              @click="sendVerifyCode('phone')"
            >
              {{ codeSending ? `${countDown}秒后重发` : `发送至${formatContact(userStore.userInfo.phone)}` }}
            </el-button>
          </el-col>
        </el-row>
      </el-form-item>
      
      <!-- 新邮箱 -->
      <el-form-item
        label="新邮箱"
        prop="newEmail"
      >
        <el-input 
          v-model="form.newEmail" 
          type="email"
          placeholder="请输入新邮箱"
        />
      </el-form-item>
      
      <!-- 新邮箱验证码 -->
      <el-form-item
        label="新邮箱验证"
        prop="newEmailCode"
      >
        <el-row :gutter="10">
          <el-col :span="14">
            <el-input 
              v-model="form.newEmailCode" 
              placeholder="请输入新邮箱验证码"
            />
          </el-col>
          <el-col :span="10">
            <el-button 
              type="text" 
              :disabled="newCodeSending || !form.newEmail"
              @click="sendNewEmailCode"
            >
              {{ newCodeSending ? `${newCountDown}秒后重发` : '发送验证码' }}
            </el-button>
          </el-col>
        </el-row>
      </el-form-item>
    </el-form>
    <!-- 对话框底部按钮 -->
    <template #footer>
      <el-button @click="isOpen = false">
        取消
      </el-button>
      <el-button
        type="primary"
        @click="submitEmailForm"
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
import { authApi } from '@/api/auth';
// import { useRouter } from 'vue-router';

// 注入全局用户状态
const userStore = useUserStore();
//导入路由
// const router = useRouter();

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
  oldPassword: '',        // 原密码
  verifyCode: '',         // 验证方式验证码
  newEmail: '',           // 新邮箱
  newEmailCode: ''        // 新邮箱验证码
}); // 表单数据

const isOpen = ref(false); // 对话框显示状态
const verifyMethod = ref('password'); // 验证方式默认值（密码验证）
const passwordError = ref(''); // 密码错误提示信息

// 验证码相关状态
const codeSending = ref(false); // 原验证方式验证码发送状态
const countDown = ref(60);      // 原验证方式倒计时
let countDownTimer = null;      // 原验证方式定时器

const newCodeSending = ref(false); // 新邮箱验证码发送状态
const newCountDown = ref(60);      // 新邮箱倒计时
let newCountDownTimer = null;      // 新邮箱定时器

// 表单校验规则
const rules = ref({
  // 原密码验证规则
  oldPassword: [
    { 
      required: () => verifyMethod.value === 'password', 
      message: '请输入原密码', 
      trigger: 'blur'
    },
    {
      validator: (rule, value, callback) => {
        if (verifyMethod.value === 'password') {
          if (passwordError.value) {
            callback(new Error('原密码输入错误'));
          } else {
            callback();
          }
        } else {
          callback();
        }
      },
      trigger: 'blur'
    }
  ],
  // 验证方式验证码规则
  verifyCode: [
    { 
      required: () => verifyMethod.value !== 'password', 
      message: `请输入${verifyMethod.value === 'email' ? '原邮箱' : '手机'}验证码`, 
      trigger: 'blur'
    },
    { 
      min: 6, 
      max: 6, 
      message: '验证码为6位', 
      trigger: 'blur'
    }
  ],
  // 新邮箱验证规则
  newEmail: [
    { 
      required: true, 
      message: '请输入新邮箱', 
      trigger: 'blur'
    },
    { 
      type: 'email', 
      message: '请输入有效的邮箱地址', 
      trigger: ['blur', 'change']
    },
    {
      validator: (rule, value, callback) => {
        if (value === userStore.userInfo.email) {
          callback(new Error('新邮箱不能与原邮箱相同'));
        } else {
          callback();
        }
      },
      trigger: 'blur'
    }
  ],
  // 新邮箱验证码规则
  newEmailCode: [
    { 
      required: true, 
      message: '请输入新邮箱验证码', 
      trigger: 'blur'
    },
    { 
      min: 6, 
      max: 6, 
      message: '验证码为6位', 
      trigger: 'blur'
    }
  ]
});

// 格式化邮箱/手机号，隐藏中间部分内容
const formatContact = (contact) => {
  if (!contact) return '';
  // 处理邮箱（如：zhangsan666@qq.com → zhang***@qq.com）
  if (contact.includes('@')) {
    const [prefix, suffix] = contact.split('@');
    return prefix.length > 3 
      ? `${prefix.slice(0, 3)}***@${suffix}` 
      : `${prefix}***@${suffix}`;
  }
  // 处理手机号（如：12345678910 → 123****8910）
  if (contact.length === 11) {
    return contact.replace(/^(\d{3})(\d{4})(\d{4})$/, '$1****$3');
  }
  return contact;
};

// 手动验证原密码
const validateOldPassword = async () => {
  if (verifyMethod.value === 'password' && form.value.oldPassword) {
    try {
      await authApi.verifyPassword({
        password: form.value.oldPassword
      });
      passwordError.value = '';
    } catch (error) {
      passwordError.value = '原密码输入错误';
    }
  }
};

// 监听验证方式变化
watch(verifyMethod, () => {
  passwordError.value = '';
  formRef.value?.clearValidate(['oldPassword', 'verifyCode']);
});

// 监听父组件的 open props，同步对话框状态
watch(() => props.open, (newVal) => {
  isOpen.value = newVal;
  if (newVal) {
    resetEmailForm(); // 打开时重置表单
  } else {
    // 关闭时清除定时器
    if (countDownTimer) clearInterval(countDownTimer);
    if (newCountDownTimer) clearInterval(newCountDownTimer);
    codeSending.value = false;
    newCodeSending.value = false;
    countDown.value = 60;
    newCountDown.value = 60;
    passwordError.value = '';
  }
}, { immediate: true });

// 监听对话框关闭事件
watch(isOpen, (newVal) => {
  emit('update:open', newVal);
});

// 重置邮箱表单
const resetEmailForm = () => {
  form.value.oldPassword = '';
  form.value.verifyCode = '';
  form.value.newEmail = '';
  form.value.newEmailCode = '';
  passwordError.value = '';
  formRef.value?.clearValidate();
};

// 发送原验证方式验证码
const sendVerifyCode = (type) => {
  if (type === 'email' && !userStore.userInfo.email) {
    ElMessage.error('未绑定邮箱，无法发送验证码');
    return;
  }
  if (type === 'phone' && !userStore.userInfo.phone) {
    ElMessage.error('未绑定手机号，无法发送验证码');
    return;
  }

  codeSending.value = true;
  
  // 模拟发送验证码接口调用
  ElMessage.success(`验证码已发送至${type === 'email' ? '原邮箱' : '手机'}`);
  
  // 倒计时逻辑
  countDownTimer = setInterval(() => {
    countDown.value--;
    if (countDown.value <= 0) {
      clearInterval(countDownTimer);
      codeSending.value = false;
      countDown.value = 60;
    }
  }, 1000);
};

// 发送新邮箱验证码
const sendNewEmailCode = () => {
  if (!form.value.newEmail) {
    ElMessage.error('请先输入新邮箱');
    return;
  }

  newCodeSending.value = true;
  
  // 模拟发送新邮箱验证码接口调用
  ElMessage.success(`验证码已发送至${form.value.newEmail}`);
  
  // 倒计时逻辑
  newCountDownTimer = setInterval(() => {
    newCountDown.value--;
    if (newCountDown.value <= 0) {
      clearInterval(newCountDownTimer);
      newCodeSending.value = false;
      newCountDown.value = 60;
    }
  }, 1000);
};

// 提交邮箱修改表单
const submitEmailForm = async () => {
  try {
    // 手动验证密码（确保红字提示正常显示）
    if (verifyMethod.value === 'password') {
      validateOldPassword();
      if (passwordError.value) {
        return;
      }
    }
    
    // 表单校验
    await formRef.value.validate();
    
    // 验证原身份（密码/验证码验证）
    let verifySuccess = false;
    switch (verifyMethod.value) {
      case 'password':
        try {
          await authApi.verifyPassword({
            password: form.value.oldPassword
          });
          verifySuccess = true;
        } catch (error) {
          verifySuccess = false;
        }
        break;
      case 'email':
      case 'phone':
        // 实际项目中这里应该验证验证码的有效性
        verifySuccess = form.value.verifyCode.length === 6; // 简化验证
        break;
    }
    
    if (!verifySuccess) {
      if (verifyMethod.value === 'password') {
        passwordError.value = '原密码输入错误';
      } else {
        ElMessage.error(`身份验证失败，请检查${verifyMethod.value === 'email' ? '原邮箱' : '手机'}验证码`);
      }
      return;
    }
    
    // 验证新邮箱验证码（实际项目中调用接口验证）
    if (form.value.newEmailCode.length !== 6) {
      ElMessage.error('新邮箱验证码验证失败');
      return;
    }
    
    // 更新用户邮箱（实际项目中调用接口）
    userStore.updateUserInfo({ email: form.value.newEmail });
    
    ElMessage.success('邮箱修改成功！');
    isOpen.value = false;
    
    // 实际项目中可能需要重新登录或刷新用户信息
    // router.push('/login');
    
  } catch (error) {
    // 密码错误时已经通过红字提示，不需要重复提示
    if (!passwordError.value) {
      ElMessage.error('邮箱修改失败，请检查表单！');
    }
    console.error('提交失败：', error);
  }
};
</script>

<style scoped>
/* 限制按钮文本容器宽度，防止溢出 */
.el-col:has(.el-button[type="text"]) {
  white-space: nowrap; /* 禁止换行 */
  overflow: hidden;    /* 隐藏溢出内容 */
  text-overflow: ellipsis; /* 溢出显示省略号 */
}

/* 调整验证码输入框与按钮的布局比例 */
.el-row {
  width: 100%; /* 确保行容器占满宽度 */
}

/* 适当调整输入框与按钮的占比 */
.el-col:nth-child(1) {
  flex: 13;
}
.el-col:nth-child(2) {
  flex: 11;
  padding-left: 8px; /* 减少间距 */
}

/* 密码错误提示样式 */
.el-form-item--error .el-input__inner {
  border-color: #f56c6c;
}

.el-form-item__error {
  color: #f56c6c;
  font-size: 12px;
  line-height: 1;
  padding-top: 4px;
}
</style>