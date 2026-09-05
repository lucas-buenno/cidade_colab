package tech.cidade.colab.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.cidade.colab.dto.request.CreateColabRequest;
import tech.cidade.colab.dto.response.ColabResponse;
import tech.cidade.colab.service.ColabService;
import tech.cidade.colab.service.SupportService;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/v1/colab")
public class ColabController {

    private final ColabService colabService;
    private final SupportService supportService;

    @PostMapping(path = "/create")
    public ResponseEntity<Void> createColab(@RequestBody CreateColabRequest request){
        String id = colabService.createColab(request);
        return ResponseEntity.created(URI.create("/v1/colab/" + id)).build();
    }

    @PutMapping(path = "/support/{colabId}/{userId}")
    public ResponseEntity<Void> supportColab(@PathVariable String colabId, @PathVariable String userId) {
        supportService.updateSupport(colabId, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping(path = "/{colabId}")
    public ResponseEntity<ColabResponse> getColab(@PathVariable String colabId) {
        ColabResponse colabResponse = colabService.getById(colabId);
        return ResponseEntity.ok(colabResponse);
    }
}
