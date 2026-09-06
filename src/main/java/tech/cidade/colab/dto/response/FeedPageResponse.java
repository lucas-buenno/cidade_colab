package tech.cidade.colab.dto.response;


import java.util.List;

public record FeedPageResponse(
        List<ColabResponse> items,
        String nextPageToken) {
}