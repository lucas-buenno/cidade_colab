package tech.cidade.colab.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tech.cidade.colab.dto.response.ColabSearchPageResponse;
import tech.cidade.colab.service.ColabSearchService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/v1/colabs")
public class ColabSearchController {

    private final ColabSearchService colabSearchService;

    @GetMapping("/search")
    public ResponseEntity<ColabSearchPageResponse> search(
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lng,
            @RequestParam(required = false) Double radiusKm,
            @RequestParam(required = false) String bbox,
            @RequestParam(required = false) List<String> category,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String pageToken,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal Jwt jwt
    ) {
        log.info("Recebendo busca de colabs - lat={} lng={} radiusKm={} bbox={} category={} q={} pageToken={} size={}",
                lat, lng, radiusKm, bbox, category, q, pageToken, size);
        String authenticatedUserId = jwt != null ? jwt.getSubject() : null;
        ColabSearchPageResponse response = colabSearchService.search(
                lat, lng, radiusKm, bbox, category, q, pageToken, size, authenticatedUserId
        );
        return ResponseEntity.ok(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleBadRequest(IllegalArgumentException exception) {
        log.warn("Busca de colabs inválida: {}", exception.getMessage());
        return ResponseEntity.badRequest().body(exception.getMessage());
    }
}
