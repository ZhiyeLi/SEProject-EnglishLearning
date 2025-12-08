require("dotenv").config();
const express = require("express");
const cors = require("cors");
const { initDatabase } = require("./scripts/initDatabase");
const { errorHandler, notFoundHandler } = require("./middleware/errorHandler");

const app = express();
const PORT = process.env.PORT || 3000;

// 中间件
app.use(
  cors({
    origin: process.env.CORS_ORIGIN || "http://localhost:8080",
    credentials: true,
  })
);
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// 请求日志
app.use((req, res, next) => {
  console.log(`[${new Date().toISOString()}] ${req.method} ${req.path}`);
  next();
});

// 健康检查
app.get("/health", (req, res) => {
  res.json({ status: "ok", message: "Server is running" });
});

// 路由
app.use("/api/auth", require("./routes/auth"));
app.use("/api/plans", require("./routes/plans"));
app.use("/api/friends", require("./routes/friends"));
app.use("/api/words", require("./routes/words"));
app.use("/api/questions", require("./routes/questions"));

// 404 处理
app.use(notFoundHandler);

// 错误处理
app.use(errorHandler);

// 初始化数据库并启动服务器
async function startServer() {
  try {
    // 初始化数据库
    await initDatabase();

    // 启动服务器
    app.listen(PORT, () => {
      console.log("=".repeat(50));
      console.log(`🚀 服务器启动成功！`);
      console.log(`📡 监听端口: ${PORT}`);
      console.log(`🌐 API地址: http://localhost:${PORT}`);
      console.log(`📊 健康检查: http://localhost:${PORT}/health`);
      console.log(`🔧 环境: ${process.env.NODE_ENV || "development"}`);
      console.log("=".repeat(50));
    });
  } catch (error) {
    console.error("❌ 服务器启动失败:", error);
    process.exit(1);
  }
}

startServer();

// 优雅关闭
process.on("SIGINT", () => {
  console.log("\n⏸️  正在关闭服务器...");
  process.exit(0);
});

process.on("SIGTERM", () => {
  console.log("\n⏸️  正在关闭服务器...");
  process.exit(0);
});
