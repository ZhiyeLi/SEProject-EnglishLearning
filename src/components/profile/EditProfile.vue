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


      <!-- 密码修改区域 -->
      <!-- <el-divider content-position="left">
        修改密码
      </el-divider> -->
      
      <!-- 验证方式选择 -->
      <!-- <el-form-item label="验证方式">
        <el-radio-group
          v-model="verifyMethod"
          @change="resetPasswordForm"
        >
          <el-radio label="password">
            输入原密码
          </el-radio>
          <el-radio label="email">
            邮箱验证
          </el-radio>
          <el-radio label="phone">
            手机验证
          </el-radio>
        </el-radio-group>
      </el-form-item> -->
      
      <!-- 原密码验证 -->
      <!-- <el-form-item 
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
      </el-form-item> -->
      
      <!-- 邮箱验证 -->
      <!-- <el-form-item 
        v-if="verifyMethod === 'email'" 
        label="验证码"
        prop="verifyCode"
      >
        <el-row :gutter="10">
          <el-col :span="14">
            <el-input 
              v-model="form.verifyCode" 
              placeholder="请输入邮箱验证码"
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
      </el-form-item> -->
      
      <!-- 手机验证 -->
      <!-- <el-form-item 
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
      </el-form-item> -->
      
      <!-- 新密码 -->
      <!-- <el-form-item
        label="新密码"
        prop="newPassword"
      >
        <el-input 
          v-model="form.newPassword" 
          type="password"
          placeholder="请输入新密码（6-20位）"
        />
      </el-form-item> -->
      
      <!-- 确认密码 -->
      <!-- <el-form-item
        label="确认密码"
        prop="confirmPassword"
      >
        <el-input 
          v-model="form.confirmPassword" 
          type="password"
          placeholder="请再次输入新密码"
        />
      </el-form-item> -->
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
import { authApi } from '@/api/auth';
// import { useRouter } from 'vue-router';
// 注入全局用户状态
const userStore = useUserStore();// const router = useRouter(); // 暂时未使用// 接收父组件传递的“是否打开对话框”参数
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
  avatar: '',
  name: '',
  signature: '',
  location: '',
  // oldPassword: '',
  // newPassword: '',
  // confirmPassword: '',
  // verifyCode: ''
}); // 表单数据
const fileList = ref([]); // 上传文件列表
const isOpen = ref(false); // 对话框显示状态

const verifyMethod = ref('password'); // 验证方式默认值（密码验证）
const codeSending = ref(false); // 验证码发送状态
const countDown = ref(60); // 倒计时秒数
let countDownTimer = null; // 倒计时定时器
const passwordError = ref(''); // 密码错误提示信息

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
  ],
  oldPassword: [
    // { required: () => verifyMethod.value === 'password', message: '请输入原密码', trigger: 'blur'},
    // {
    //   validator: (rule,value,callback) =>{
    //     // 只有选择"输入原密码"方式时才验证
    //     if (verifyMethod.value === 'password') {
    //       if (passwordError.value) {
    //         callback(new Error('原密码输入错误'));
    //       } else {
    //         callback();
    //       }
    //     } else {
    //       callback();
    //     }
    //   }
    // }
  ],
  verifyCode: [
    // { required: verifyMethod.value !== 'password', message: '请输入验证码', trigger: 'blur'},
    // { min: 6,max: 6,message: '验证码为6位', trigger: 'blur'}
  ],
  // newPassword: [
  //   { required: true,message: '请输入新密码', trigger: 'blur'},
  //   { min: 6,max: 20,message: '密码长度在6-20之间', trigger: 'blur'}
  // ],
  // confirmPassword: [
  //   { required: true,message: '请确认新密码', trigger: 'blur'},
  //   { 
  //     validator: (rule,value,callback) =>{
  //       if(value !==form.value.newPassword){
  //         callback(new Error('两次输入的密码不一致'));
  //       }
  //       else{
  //         callback();
  //       }
  //     },
  //     trigger: 'blur'
  //   }
  // ],
});

// /*
// 格式化邮箱/手机号，隐藏中间部分内容
// eslint-disable-next-line no-unused-vars
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
// */

// 监听验证方式变化
watch(verifyMethod, () => {
  passwordError.value = '';
  formRef.value.clearValidate(['oldPassword', 'verifyCode']);
});

// /*
// 手动验证原密码
// eslint-disable-next-line no-unused-vars
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
// */

// 重置密码表单（由于密码字段被注释，暂时保留但不执行）
// const resetPasswordForm = () => {
//   form.value.oldPassword = '';
//   form.value.verifyCode = '';
//   form.value.newPassword = '';
//   form.value.confirmPassword = '';
//   passwordError.value = '';
// };

// /*
// 发送验证码
// eslint-disable-next-line no-unused-vars
const sendVerifyCode = (type) => {
  // 模拟发送验证码
  codeSending.value = true;
  
  // 实际项目中这里调用后端发送验证码接口
  ElMessage.success(`验证码已发送至${type === 'email' ? userStore.userInfo.email : userStore.userInfo.phone}`);
  
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
// */

// 监听父组件的 open  props，同步对话框状态并回显数据
watch(() => props.open, (newVal) => {
  isOpen.value = newVal;
  // 1. 复制用户信息（包含所有字段）
    const userInfo = { ...userStore.userInfo };
    // 2. 删除 password 字段（不回显密码）
    delete userInfo.password;
    // 3. 赋值给表单，同时重置密码相关字段
    form.value = { ...userInfo };
    // resetPasswordForm(); // 密码字段被注释，暂时禁用 
}, { immediate: true });

// 监听对话框关闭，向父组件发送关闭事件
watch(isOpen, (newVal) => {
  emit('update:open', newVal);
  if (!newVal && countDownTimer) {
    clearInterval(countDownTimer);
    codeSending.value = false;
    countDown.value = 60;
    passwordError.value = '';
  }
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
     // 准备更新的数据
    const updateData = { ...form.value };
    // 移除密码相关字段（由于密码字段被注释，实际上已不存在）
    // const { newPassword, ...userInfo } = updateData;
    // 调用Pinia的action更新用户信息
    userStore.updateUserInfo(updateData);
    
     // 处理密码修改（如果有新密码）
     // 由于密码字段被注释，暂时禁用密码修改功能
     // 在需要时可以取消注释恢复功能
     // if (newPassword) {
     //   // 由于验证方式被注释，暂时跳过密码验证，直接修改密码
     //   // 在实际使用时需要取消注释验证方式并启用验证
     //   // if (verifyMethod.value === 'password') {
     //   //   try {
     //   //     await authApi.verifyPassword({
     //   //       password: form.value.oldPassword
     //   //     });
     //   //   } catch (error) {
     //   //     ElMessage.error('原密码验证失败');
     //   //     return;
     //   //   }
     //   // }
     //   
     //   // 2. 调用密码修改接口
     //   await authApi.changePassword({
     //     oldPassword: form.value.oldPassword,
     //     newPassword: form.value.newPassword
     //   });
     //   
     //   // 3. 密码修改成功后需要重新登录，不需要更新前端状态
     //   
     //   // 重置密码表单
     //   resetPasswordForm();
     //   
     //   ElMessage.success('密码修改成功，请重新登录');
     //   // 实际项目中这里需要跳转到登录页
     //   router.push('/login');
     // }
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

/* 适当调整输入框与按钮的占比（原14:10 → 13:11） */
.el-col:nth-child(1) {
  flex: 13;
}
.el-col:nth-child(2) {
  flex: 11;
  padding-left: 8px; /* 减少间距 */
}
</style>