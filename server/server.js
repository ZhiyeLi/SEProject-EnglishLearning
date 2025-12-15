require("dotenv").config();
const express = require("express");
const cors = require("cors");
const http = require("http");
const jwt = require("jsonwebtoken");
const { initDatabase } = require("./scripts/initDatabase");
const { errorHandler, notFoundHandler } = require("./middleware/errorHandler");
const { dbRun, dbGet } = require("./config/database");

const app = express();
const PORT = process.env.PORT || 3000;

// Socket user map: userId => Set(socketIds)
const onlineUsers = new Map();

// 中间件
app.use(
  cors({
    origin: function (origin, callback) {
      // 允许的源列表
      const allowedOrigins = [
        "http://localhost:8080",
        "http://localhost:8081",
        "http://127.0.0.1:8080",
        "http://127.0.0.1:8081",
      ];
      
      // 允许来自局域网的请求（192.168.x.x 和 10.x.x.x）
      const isLocalNetwork =
        origin &&
        (origin.startsWith("http://192.168.") ||
          origin.startsWith("http://10.") ||
          origin.startsWith("http://172."));
      
      // 如果是允许列表中的源，或者来自局域网，允许请求
      if (!origin || allowedOrigins.includes(origin) || isLocalNetwork) {
        callback(null, true);
      } else {
        callback(new Error("CORS policy: origin not allowed"));
      }
    },
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

// 创建 http server 并挂载 Socket.IO
const server = http.createServer(app);
const { Server } = require("socket.io");
const io = new Server(server, {
  cors: {
    origin: function (origin, callback) {
      // 允许的源列表
      const allowedOrigins = [
        "http://localhost:8080",
        "http://localhost:8081",
        "http://127.0.0.1:8080",
        "http://127.0.0.1:8081",
      ];
      
      // 允许来自局域网的请求（192.168.x.x 和 10.x.x.x）
      const isLocalNetwork =
        origin &&
        (origin.startsWith("http://192.168.") ||
          origin.startsWith("http://10.") ||
          origin.startsWith("http://172."));
      
      // 如果是允许列表中的源，或者来自局域网，允许请求
      if (!origin || allowedOrigins.includes(origin) || isLocalNetwork) {
        callback(null, true);
      } else {
        callback(new Error("CORS policy: origin not allowed"));
      }
    },
    credentials: true,
  },
});

// Socket.IO 连接处理
io.on("connection", async (socket) => {
  try {
    // token 可以通过 handshake.auth 或 query 传入
    const token = socket.handshake.auth?.token || socket.handshake.query?.token;
    if (!token) {
      socket.disconnect(true);
      return;
    }

    let decoded;
    try {
      decoded = jwt.verify(token, process.env.JWT_SECRET || "your-secret-key-change-in-production");
    } catch (err) {
      socket.disconnect(true);
      return;
    }

    const userId = decoded.userId;
    socket.userId = userId;

    // 将 socketId 加入 onlineUsers
    if (!onlineUsers.has(userId)) onlineUsers.set(userId, new Set());
    onlineUsers.get(userId).add(socket.id);

    console.log(`Socket connected: userId=${userId}, socketId=${socket.id}`);

    // 监听发送消息事件
    socket.on("send_message", async (payload) => {
      try {
        const senderId = socket.userId;
        const { receiverId, content } = payload || {};
        if (!receiverId || !content) return;

        // 插入消息到数据库
        const result = await dbRun(
          "INSERT INTO messages (sender_id, receiver_id, content) VALUES (?, ?, ?)",
          [senderId, receiverId, content]
        );

        // 读取刚插入的消息
        const message = await dbGet("SELECT * FROM messages WHERE message_id = ?", [result.lastID]);

        // 发送给接收者（如果在线）
        const receivers = onlineUsers.get(String(receiverId)) || new Set();
        receivers.forEach((sid) => {
          io.to(sid).emit("receive_message", message);
        });

        // 同时回传给发送者（确认）
        socket.emit("message_sent", message);
      } catch (err) {
        console.error("Socket send_message error:", err);
      }
    });

    socket.on("disconnect", () => {
      // 从 onlineUsers 移除
      const set = onlineUsers.get(userId);
      if (set) {
        set.delete(socket.id);
        if (set.size === 0) onlineUsers.delete(userId);
      }
      console.log(`Socket disconnected: userId=${userId}, socketId=${socket.id}`);
    });
  } catch (error) {
    console.error("Socket connection error:", error);
  }
});

// 404 处理
app.use(notFoundHandler);

// 错误处理
app.use(errorHandler);

// 初始化数据库并启动服务器（含 Socket.IO）
async function startServer() {
  try {
    // 初始化数据库
    await initDatabase();

    // 启动 http server（Socket.IO 已挂载）
    server.listen(PORT, "0.0.0.0", () => {
      const os = require("os");
      const interfaces = os.networkInterfaces();
      let ipAddress = "localhost";
      
      // 获取第一个非本地网络接口的 IP
      for (const name of Object.keys(interfaces)) {
        for (const iface of interfaces[name]) {
          if (iface.family === "IPv4" && !iface.internal) {
            ipAddress = iface.address;
            break;
          }
        }
        if (ipAddress !== "localhost") break;
      }
      
      console.log("=".repeat(50));
      console.log(`🚀 服务器启动成功！`);
      console.log(`📡 监听端口: ${PORT}`);
      console.log(`🌐 本机访问: http://localhost:${PORT}`);
      console.log(`🌐 局域网访问: http://${ipAddress}:${PORT}`);
      console.log(`🔧 环境: ${process.env.NODE_ENV || "development"}`);
      console.log("Socket.IO 已启用");
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
