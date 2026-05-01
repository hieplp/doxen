package dev.hieplp.doxen.adapter.in.api.statuscode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode implements StatusCode {
    INVALID_TOKEN("4001", "Invalid token"),
    ROOT_ALREADY_EXISTS("4002", "Root account already exists"),
    INVALID_CREDENTIALS("4003", "Invalid credentials"),
    ;

    private final String code;
    private final String message;
}
