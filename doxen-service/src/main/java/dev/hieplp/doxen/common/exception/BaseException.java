package dev.hieplp.doxen.common.exception;

public abstract class BaseException extends RuntimeException {

    protected BaseException() {
    }

    protected BaseException(String message) {
        super(message);
    }
}
