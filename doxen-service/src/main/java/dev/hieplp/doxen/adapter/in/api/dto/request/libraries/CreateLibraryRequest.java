package dev.hieplp.doxen.adapter.in.api.dto.request.libraries;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CreateLibraryRequest(
        @NotBlank @Pattern(regexp = ".*[A-Za-z0-9].*", message = "must contain at least one letter or digit") String name,
        String description,
        String homepageUrl
) {
}
