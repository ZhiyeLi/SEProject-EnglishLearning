package com.example.english_learning_platform.util;

import lombok.Data;

@Data
public class Result {
    private int code;  // 200成功，其他失败
    private String message;
    private Object data;

    public static Result success(Object data) {
        Result result = new Result();
        result.setCode(200);
        result.setData(data);
        return result;
    }

    public static Result error(String message) {
        Result result = new Result();
        result.setCode(400);
        result.setMessage(message);
        return result;
    }
}