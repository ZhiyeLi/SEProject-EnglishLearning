/**
 * 统一响应格式工具
 */
class ResponseUtil {
  /**
   * 成功响应
   */
  static success(res, data = null, message = "操作成功", code = 200) {
    return res.status(200).json({
      code,
      message,
      data,
    });
  }

  /**
   * 失败响应
   */
  static error(res, message = "操作失败", code = 400, data = null) {
    return res.status(code >= 500 ? 500 : 200).json({
      code,
      message,
      data,
    });
  }

  /**
   * 分页响应
   */
  static paginate(res, data, total, page, pageSize, message = "查询成功") {
    return res.status(200).json({
      code: 200,
      message,
      data: {
        list: data,
        total,
        page: parseInt(page),
        pageSize: parseInt(pageSize),
        totalPages: Math.ceil(total / pageSize),
      },
    });
  }
}

module.exports = ResponseUtil;
