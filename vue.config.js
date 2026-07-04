const { defineConfig } = require("@vue/cli-service");

module.exports = defineConfig({
  transpileDependencies: true,
  devServer: {
    host: "0.0.0.0", // 监听所有网卡
    port: 8081,
    proxy: {
      "/api": {
        target: "http://localhost:9090",
        changeOrigin: true,
        ws: true,
      },
      "/rag-sse": {
        target: "http://localhost:8001",
        changeOrigin: true,
        pathRewrite: { "^/rag-sse": "" },
      },
    },
  },
});
