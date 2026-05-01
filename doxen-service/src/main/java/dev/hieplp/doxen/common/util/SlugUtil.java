package dev.hieplp.doxen.common.util;

public final class SlugUtil {

    private SlugUtil() {
    }

    public static String toSlug(String value) {
        return value.toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "");
    }
}
