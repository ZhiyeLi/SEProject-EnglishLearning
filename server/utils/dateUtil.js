// 获取本周（周一00:00 至 周日23:59:59）的时间范围（ISO字符串）
exports.getWeekTimeRange = () => {
  const now = new Date();
  // 计算本周一
  const monday = new Date(now);
  monday.setDate(now.getDate() - (now.getDay() || 7) + 1);
  monday.setHours(0, 0, 0, 0);
  // 计算本周日
  const sunday = new Date(monday);
  sunday.setDate(monday.getDate() + 6);
  sunday.setHours(23, 59, 59, 999);
  // 转为SQLite兼容的字符串格式
  return {
    monday: monday.toISOString().slice(0, 19).replace('T', ' '),
    sunday: sunday.toISOString().slice(0, 19).replace('T', ' ')
  };
};