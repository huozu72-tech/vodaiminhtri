package com.example.chieuthu3_ketthucmonhoc.domain.services;

public class AuthResult<T> {
    private final boolean isSuccess;
    private final T data;
    private final String message;

    private AuthResult(boolean isSuccess, T data, String message) {
        this.isSuccess = isSuccess;
        this.data = data;
        this.message = message;
    }

    public static <T> AuthResult<T> success(T data) {
        return new AuthResult<>(true, data, null);
    }

    public static <T> AuthResult<T> error(String message) {
        return new AuthResult<>(false, null, message);
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public T getData() {
        return data;
    }

    public String getMessage() {
        return message;
    }
}
