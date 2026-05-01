package dev.hieplp.doxen.adapter.in.api.statuscode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SuccessCode implements StatusCode {
    SUCCESS("2000", "Success"),
    ;

    private final String code;
    private final String message;

}
