const { defineConfig } = require("@vue/cli-service");

module.exports = defineConfig({
  transpileDependencies: true,
  devServer: {
    host: "0.0.0.0", // 监听所有网卡
    port: 8081,
    proxy: {
      "/api": {
        target: "http://localhost:8080",
        changeOrigin: true,
        ws: true,
        pathRewrite: {
          // 如果后端API路径已包含/api,则不需要重写
          // '^/api': ''
        },
      },
    },
  },
});
