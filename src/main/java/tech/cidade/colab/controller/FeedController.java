package tech.cidade.colab.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tech.cidade.colab.dto.response.FeedPageResponse;
import tech.cidade.colab.service.FeedService;


@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("v1/feed")
public class FeedController {

    private final FeedService feedService;

    @GetMapping
    public ResponseEntity<FeedPageResponse> getFeed(
            @RequestParam(required = false) String pageToken,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal Jwt jwt) {
        log.info("Recebendo requisição para buscar feed de colabs - pageToken: {} - size: {}", pageToken, size);
        String authenticatedUserId = jwt != null ? jwt.getSubject() : null;
        FeedPageResponse feed = feedService.getFeed(pageToken, size, authenticatedUserId);
        return ResponseEntity.ok(feed);
    }
}
