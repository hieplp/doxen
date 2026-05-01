package dev.hieplp.doxen.domain.vo;

public record UserId(String value) {
    @Override
    public String toString() {
        return value;
    }
}
