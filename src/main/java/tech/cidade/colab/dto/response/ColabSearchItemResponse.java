package tech.cidade.colab.dto.response;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

public record ColabSearchItemResponse(
        @JsonUnwrapped ColabResponse colab,
        Long distanceMeters) {
}
