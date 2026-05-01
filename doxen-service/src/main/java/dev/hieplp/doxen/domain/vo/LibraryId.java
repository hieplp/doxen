package dev.hieplp.doxen.domain.vo;

public record LibraryId(String value) {
    @Override
    public String toString() {
        return value;
    }
}
