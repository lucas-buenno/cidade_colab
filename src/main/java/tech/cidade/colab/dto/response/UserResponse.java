package tech.cidade.colab.dto.response;


import java.time.Instant;
import java.util.List;

public record UserResponse(String username, Instant createdAt, List<ColabResponse> userColabs) {
}
