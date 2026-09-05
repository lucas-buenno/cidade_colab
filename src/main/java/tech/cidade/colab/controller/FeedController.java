package tech.cidade.colab.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.cidade.colab.dto.response.ColabResponse;
import tech.cidade.colab.service.FeedService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("v1/feed")
public class FeedController {

    private final FeedService feedService;

    @GetMapping
    public ResponseEntity<List<ColabResponse>> getFeed() {
        log.info("Recebendo requisição para buscar feed de colabs");
        List<ColabResponse> feed = feedService.getFeed();
        return ResponseEntity.ok(feed);
    }
}
