package tech.cidade.colab.dto.response;

import tech.cidade.colab.dto.Location;
import tech.cidade.colab.enums.EColabStatus;

import java.time.Instant;
import java.util.List;

public record ColabResponse(
        String id,
        String userId,
        String username,
        String title,
        String description,
        List<CategoryResponse> categories,
        EColabStatus status,
        Integer supportCount,
        boolean supportedByMe,
        Location location,
        Instant createdAt,
        Instant updatedAt,
        String imageUrl) {
}
