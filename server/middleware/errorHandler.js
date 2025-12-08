/**
 * 错误处理中间件
 */
const errorHandler = (err, req, res, next) => {
  console.error("错误:", err);

  // SQLite 错误处理
  if (err.code === "SQLITE_CONSTRAINT") {
    return res.status(400).json({
      code: 400,
      message: "数据约束冲突",
      error: err.message,
    });
  }

  // 默认错误处理
  res.status(err.status || 500).json({
    code: err.status || 500,
    message: err.message || "服务器内部错误",
    ...(process.env.NODE_ENV === "development" && { stack: err.stack }),
  });
};

/**
 * 404 处理中间件
 */
const notFoundHandler = (req, res) => {
  res.status(404).json({
    code: 404,
    message: "请求的资源不存在",
  });
};

module.exports = {
  errorHandler,
  notFoundHandler,
};
