package dev.hieplp.doxen.adapter.in.security.constants;

import java.util.List;

public final class SecurityPaths {

    public static final String REFRESH_TOKEN_PATH = "/api/v1/auth/refresh";

    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/v1/root",
            "/api/v1/auth/login",
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/v3/api-docs/**"
    );

    private SecurityPaths() {
    }

    public static List<String> publicPaths() {
        return PUBLIC_PATHS;
    }

    public static String[] publicPathArray() {
        return PUBLIC_PATHS.toArray(String[]::new);
    }
}
