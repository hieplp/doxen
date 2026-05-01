package dev.hieplp.doxen.adapter.in.api.dto.response.base;

import dev.hieplp.doxen.adapter.in.api.statuscode.StatusCode;
import dev.hieplp.doxen.adapter.in.api.statuscode.SuccessCode;
import lombok.Data;

@Data
public class ApiResponse<T> {
    private String code;
    private String message;
    private T data;

    public ApiResponse(StatusCode statusCode, T data) {
        this.code = statusCode.getCode();
        this.message = statusCode.getMessage();
        this.data = data;
    }

    public ApiResponse(StatusCode statusCode) {
        this(statusCode, null);
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<T>(SuccessCode.SUCCESS, data);
    }

    public static <T> ApiResponse<T> success() {
        return new ApiResponse<T>(SuccessCode.SUCCESS, null);
    }

}
