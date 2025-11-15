// .eslintrc.js（最终正确配置，只保留Vue 3必需的）
module.exports = {
  root: true,
  env: {
    node: true,
    browser: true,
    es2021: true,
  },
  parser: 'vue-eslint-parser', // 解析Vue组件
  parserOptions: {
    parser: '@babel/eslint-parser', // 解析JS代码
    ecmaVersion: 'latest',
    sourceType: 'module',
  },
  extends: [
    'eslint:recommended',
    'plugin:vue/vue3-recommended' // 只保留Vue 3推荐规则（去掉prettier相关）
  ],
  rules: {
    'no-undef': 'off', // 允许Vue编译器宏（defineProps等）
    'no-unused-vars': ['error', { varsIgnorePattern: 'props|emit' }], // 忽略props/emit未使用警告
    'vue/multi-word-component-names': 'off' // 允许单单词组件名
  }
};