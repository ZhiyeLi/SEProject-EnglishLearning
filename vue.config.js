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
        router: function(req) {
          // 获取请求的 Host 头
          const host = req.headers.host; // 例如 "192.168.1.100:8081"
          if (host) {
            // 提取主机名（去掉端口）
            const hostname = host.split(':')[0];
            // 返回动态的后端地址
            return `http://${hostname}:8080`;
          }
          return "http://localhost:8080";
        },
        pathRewrite: {
          // 如果后端API路径已包含/api,则不需要重写
          // '^/api': ''
        },
      },
    },
  },
});
