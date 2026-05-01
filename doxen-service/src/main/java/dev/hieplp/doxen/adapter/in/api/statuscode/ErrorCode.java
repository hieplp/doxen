package dev.hieplp.doxen.adapter.in.api.statuscode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode implements StatusCode {
    INVALID_TOKEN("4001", "Invalid token"),
    UNAUTHORIZED("401", "Unauthorized"),
    FORBIDDEN("403", "Forbidden"),
    BAD_REQUEST("400", "Bad request"),
    NOT_FOUND("404", "Resource not found"),
    INTERNAL_SERVER_ERROR("500", "Internal server error"),
    ROOT_ALREADY_EXISTS("4002", "Root account already exists"),
    INVALID_CREDENTIALS("4003", "Invalid credentials"),
    LIBRARY_DUPLICATE_SLUG("4004", "Library with this slug already exists"),
    LIBRARY_NOT_FOUND("4005", "Library not found"),
    ;

    private final String code;
    private final String message;
}
