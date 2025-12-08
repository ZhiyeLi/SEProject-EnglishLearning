const jwt = require("jsonwebtoken");

/**
 * JWT 认证中间件
 */
const authMiddleware = (req, res, next) => {
  try {
    // 从请求头获取 token
    const token = req.headers.authorization?.replace("Bearer ", "");

    if (!token) {
      return res.status(401).json({
        code: 401,
        message: "未提供认证令牌",
      });
    }

    // 验证 token
    const decoded = jwt.verify(
      token,
      process.env.JWT_SECRET || "your-secret-key-change-in-production"
    );

    // 将用户信息附加到请求对象
    req.userId = decoded.userId;
    req.user = decoded;

    next();
  } catch (error) {
    if (error.name === "TokenExpiredError") {
      return res.status(401).json({
        code: 401,
        message: "认证令牌已过期",
      });
    }

    return res.status(401).json({
      code: 401,
      message: "无效的认证令牌",
    });
  }
};

/**
 * 可选认证中间件（token 存在则验证，不存在则跳过）
 */
const optionalAuthMiddleware = (req, res, next) => {
  try {
    const token = req.headers.authorization?.replace("Bearer ", "");

    if (token) {
      const decoded = jwt.verify(
        token,
        process.env.JWT_SECRET || "your-secret-key-change-in-production"
      );
      req.userId = decoded.userId;
      req.user = decoded;
    }

    next();
  } catch (error) {
    // 认证失败但继续执行
    next();
  }
};

module.exports = {
  authMiddleware,
  optionalAuthMiddleware,
};
