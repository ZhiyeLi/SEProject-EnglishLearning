const bcrypt = require("bcryptjs");
const jwt = require("jsonwebtoken");
const { dbRun, dbGet, dbAll } = require("../config/database");
const ResponseUtil = require("../utils/response");

/**
 * 用户注册
 */
const register = async (req, res) => {
  try {
    const { username, password, email } = req.body;

    // 验证必填字段
    if (!username || !password || !email) {
      return ResponseUtil.error(res, "用户名、密码和邮箱均为必填项", 400);
    }

    // 检查邮箱是否已存在
    const existingUser = await dbGet(
      "SELECT user_id FROM users WHERE user_email = ?",
      [email]
    );
    if (existingUser) {
      return ResponseUtil.error(res, "该邮箱已被注册", 400);
    }

    // 加密密码
    const hashedPassword = await bcrypt.hash(password, 10);

    // 插入新用户
    const result = await dbRun(
      `INSERT INTO users (user_name, user_password, user_email) VALUES (?, ?, ?)`,
      [username, hashedPassword, email]
    );

    return ResponseUtil.success(res, { userId: result.lastID }, "注册成功");
  } catch (error) {
    console.error("注册失败:", error);
    return ResponseUtil.error(res, "注册失败", 500);
  }
};

/**
 * 用户登录
 */
const login = async (req, res) => {
  try {
    const { username, password } = req.body;

    if (!username || !password) {
      return ResponseUtil.error(res, "用户名和密码不能为空", 400);
    }

    // 查询用户（支持用户名或邮箱登录）
    const user = await dbGet(
      "SELECT * FROM users WHERE user_name = ? OR user_email = ?",
      [username, username]
    );

    if (!user) {
      return ResponseUtil.error(res, "用户名或密码错误", 401);
    }

    // 验证密码
    const isPasswordValid = await bcrypt.compare(password, user.user_password);
    if (!isPasswordValid) {
      return ResponseUtil.error(res, "用户名或密码错误", 401);
    }

    // 生成 JWT token
    const token = jwt.sign(
      { userId: user.user_id, email: user.user_email },
      process.env.JWT_SECRET || "your-secret-key-change-in-production",
      { expiresIn: process.env.JWT_EXPIRE || "7d" }
    );

    // 返回用户信息（不包含密码）
    const userInfo = {
      userId: user.user_id,
      name: user.user_name,
      email: user.user_email,
      avatar: user.avatar,
      status: user.user_status,
      signature: user.signature,
      streak: user.streak,
      joinTime: user.created_at,
    };

    return ResponseUtil.success(res, { token, userInfo }, "登录成功");
  } catch (error) {
    console.error("登录失败:", error);
    return ResponseUtil.error(res, "登录失败", 500);
  }
};

/**
 * 发送验证码（模拟实现）
 */
const sendVerifyCode = async (req, res) => {
  try {
    const { account, type } = req.body;

    if (!account || !type) {
      return ResponseUtil.error(res, "账号和类型不能为空", 400);
    }

    // 验证账号是否存在
    const field = type === "email" ? "user_email" : "user_name";
    const user = await dbGet(`SELECT user_id FROM users WHERE ${field} = ?`, [
      account,
    ]);

    if (!user) {
      return ResponseUtil.error(res, "账号不存在", 404);
    }

    // TODO: 实际项目中应调用短信或邮件服务发送验证码
    // 这里模拟验证码为 123456
    console.log(`验证码已发送到 ${account}: 123456`);

    return ResponseUtil.success(res, null, "验证码发送成功");
  } catch (error) {
    console.error("发送验证码失败:", error);
    return ResponseUtil.error(res, "验证码发送失败", 500);
  }
};

/**
 * 重置密码
 */
const resetPassword = async (req, res) => {
  try {
    const { account, type, verifyCode, newPassword } = req.body;

    if (!account || !type || !verifyCode || !newPassword) {
      return ResponseUtil.error(res, "所有字段均为必填", 400);
    }

    // TODO: 实际项目中应验证验证码的有效性
    // 这里简化处理，假设验证码为 123456
    if (verifyCode !== "123456") {
      return ResponseUtil.error(res, "验证码错误", 400);
    }

    // 查找用户
    const field = type === "email" ? "user_email" : "user_name";
    const user = await dbGet(`SELECT user_id FROM users WHERE ${field} = ?`, [
      account,
    ]);

    if (!user) {
      return ResponseUtil.error(res, "账号不存在", 404);
    }

    // 加密新密码
    const hashedPassword = await bcrypt.hash(newPassword, 10);

    // 更新密码
    await dbRun(
      "UPDATE users SET user_password = ?, updated_at = CURRENT_TIMESTAMP WHERE user_id = ?",
      [hashedPassword, user.user_id]
    );

    return ResponseUtil.success(res, null, "密码重置成功");
  } catch (error) {
    console.error("重置密码失败:", error);
    return ResponseUtil.error(res, "密码重置失败", 500);
  }
};

/**
 * 获取当前用户信息
 */
const getCurrentUser = async (req, res) => {
  try {
    const user = await dbGet(
      "SELECT user_id, user_name, user_email, avatar, user_status, signature, streak, created_at FROM users WHERE user_id = ?",
      [req.userId]
    );

    if (!user) {
      return ResponseUtil.error(res, "用户不存在", 404);
    }

    const userInfo = {
      userId: user.user_id,
      name: user.user_name,
      email: user.user_email,
      avatar: user.avatar,
      status: user.user_status,
      signature: user.signature,
      streak: user.streak,
      joinTime: user.created_at,
    };

    return ResponseUtil.success(res, userInfo);
  } catch (error) {
    console.error("获取用户信息失败:", error);
    return ResponseUtil.error(res, "获取用户信息失败", 500);
  }
};

/**
 * 更新用户信息
 */
const updateUserInfo = async (req, res) => {
  try {
    const { name, avatar, status, signature } = req.body;
    const updates = [];
    const params = [];

    if (name !== undefined) {
      updates.push("user_name = ?");
      params.push(name);
    }
    if (avatar !== undefined) {
      updates.push("avatar = ?");
      params.push(avatar);
    }
    if (status !== undefined) {
      updates.push("user_status = ?");
      params.push(status);
    }
    if (signature !== undefined) {
      updates.push("signature = ?");
      params.push(signature);
    }

    if (updates.length === 0) {
      return ResponseUtil.error(res, "没有要更新的字段", 400);
    }

    updates.push("updated_at = CURRENT_TIMESTAMP");
    params.push(req.userId);

    await dbRun(
      `UPDATE users SET ${updates.join(", ")} WHERE user_id = ?`,
      params
    );

    return ResponseUtil.success(res, null, "用户信息更新成功");
  } catch (error) {
    console.error("更新用户信息失败:", error);
    return ResponseUtil.error(res, "更新用户信息失败", 500);
  }
};

/**
 * 修改密码
 */
const changePassword = async (req, res) => {
  try {
    const { oldPassword, newPassword } = req.body;

    if (!oldPassword || !newPassword) {
      return ResponseUtil.error(res, "旧密码和新密码不能为空", 400);
    }

    // 获取当前用户
    const user = await dbGet(
      "SELECT user_password FROM users WHERE user_id = ?",
      [req.userId]
    );

    if (!user) {
      return ResponseUtil.error(res, "用户不存在", 404);
    }

    // 验证旧密码
    const isPasswordValid = await bcrypt.compare(
      oldPassword,
      user.user_password
    );
    if (!isPasswordValid) {
      return ResponseUtil.error(res, "旧密码错误", 401);
    }

    // 加密新密码
    const hashedPassword = await bcrypt.hash(newPassword, 10);

    // 更新密码
    await dbRun(
      "UPDATE users SET user_password = ?, updated_at = CURRENT_TIMESTAMP WHERE user_id = ?",
      [hashedPassword, req.userId]
    );

    return ResponseUtil.success(res, null, "密码修改成功");
  } catch (error) {
    console.error("修改密码失败:", error);
    return ResponseUtil.error(res, "修改密码失败", 500);
  }
};

module.exports = {
  register,
  login,
  sendVerifyCode,
  resetPassword,
  getCurrentUser,
  updateUserInfo,
  changePassword,
};
