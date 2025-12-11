const { defineConfig } = require('@vue/cli-service')
module.exports = defineConfig({
  transpileDependencies: true,
  // 添加代理配置
  devServer: {
    proxy: {
      // 匹配所有以 /api 开头的请求路径
      '/api': {
        target: 'http://localhost:8081', // 后端API的基础路径（根据实际后端地址修改）
        changeOrigin: true, // 允许跨域（关键配置）
        // 路径重写规则：
        // 如果后端接口本身不带 /api 前缀，则需要去掉，例如：
        // pathRewrite: { '^/api': '' }
        // 如果后端接口带 /api 前缀，则保持默认（不需要重写）
      }
    }
  }
})