package dev.hieplp.doxen.adapter.in.security.filter;

import dev.hieplp.doxen.adapter.in.security.constants.SecurityPaths;
import dev.hieplp.doxen.application.port.out.token.ValidateTokenPort;
import dev.hieplp.doxen.domain.constants.TokenConstant;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JwtTokenTypeFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    private final ValidateTokenPort validateTokenPort;
    private final AuthenticationEntryPoint authenticationEntryPoint;

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        return SecurityPaths.publicPaths().stream()
                .anyMatch(path -> PATH_MATCHER.match(path, request.getServletPath()));
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        final var bearerToken = extractBearerToken(request);
        if (bearerToken.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        if (!hasExpectedTokenType(request, bearerToken.get())) {
            reject(request, response);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private Optional<String> extractBearerToken(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(HttpHeaders.AUTHORIZATION))
                .filter(header -> header.startsWith(BEARER_PREFIX))
                .map(header -> header.substring(BEARER_PREFIX.length()));
    }

    private boolean hasExpectedTokenType(HttpServletRequest request, String token) {
        return validateTokenPort.validate(token)
                .map(claims -> expectedTokenType(request).equals(claims.type()))
                .orElse(false);
    }

    private String expectedTokenType(HttpServletRequest request) {
        return SecurityPaths.REFRESH_TOKEN_PATH.equals(request.getServletPath())
                ? TokenConstant.REFRESH_TOKEN_TYPE
                : TokenConstant.ACCESS_TOKEN_TYPE;
    }

    private void reject(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        authenticationEntryPoint.commence(request, response, new BadCredentialsException("Invalid token"));
    }
}
