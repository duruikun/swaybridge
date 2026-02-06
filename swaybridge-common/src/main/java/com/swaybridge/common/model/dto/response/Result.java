package com.swaybridge.common.model.dto.response;

import lombok.Data;
import java.io.Serializable;

/**
 * 统一接口返回封装类（泛型版）
 */
@Data
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 业务状态码（通常 200=成功，其他为业务错误码）
     */
    private int code;

    /**
     * 状态描述（success / 失败原因简述）
     */
    private String message;

    /**
     * 返回的数据（泛型，支持任意类型）
     */
    private T data;

    /**
     * 时间戳（可选，方便排查问题）
     */
    private long timestamp = System.currentTimeMillis();

    // ------------------ 成功场景 ------------------

    public static <T> Result<T> ok() {
        return ok(null, "操作成功");
    }

    public static <T> Result<T> ok(T data) {
        return ok(data, "操作成功");
    }

    public static <T> Result<T> ok(T data, String message) {
        Result<T> response = new Result<>();
        response.setCode(200);
        response.setMessage(message);
        response.setData(data);
        return response;
    }

    // ------------------ 失败场景 ------------------

    public static <T> Result<T> fail(int code, String message) {
        Result<T> response = new Result<>();
        response.setCode(code);
        response.setMessage(message);
        return response;
    }

    public static <T> Result<T> fail(String message) {
        return fail(400, message);
    }

    public static <T> Result<T> error(String message) {
        return fail(500, message);
    }

    // 判断是否成功（前端/后端通用工具方法）
    public boolean isSuccess() {
        return this.code == 200;
    }
}