package tech.cidade.colab.dto.response;

import java.util.List;

public record ColabSearchPageResponse(
        List<ColabSearchItemResponse> items,
        String nextPageToken) {
}
