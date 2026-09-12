package tech.cidade.colab.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tech.cidade.colab.dto.request.CreateColabRequest;
import tech.cidade.colab.dto.response.ColabResponse;
import tech.cidade.colab.dto.response.UploadImageResponse;
import tech.cidade.colab.dto.response.UserResponse;
import tech.cidade.colab.service.ColabService;
import tech.cidade.colab.service.SupportService;
import tech.cidade.colab.service.CloudinaryService;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/v1/colab")
public class ColabController {

    private final ColabService colabService;
    private final SupportService supportService;
    private final CloudinaryService uploadService;

    @PostMapping(path = "/prepare")
    public ResponseEntity<UploadImageResponse> prepareColabImageUpload(@RequestParam("file") MultipartFile file) {
        try {
            byte[] fileBytes = file.getBytes();
            String fileName = file.getOriginalFilename();
            UploadImageResponse uploadImageResponse = uploadService.uploadFile(fileBytes, fileName);
            return ResponseEntity.ok(uploadImageResponse);
        } catch (Exception e) {
            log.error("Erro ao fazer upload do arquivo: {}", file.getOriginalFilename(), e);
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping(path = "/create")
    public ResponseEntity<Void> createColab(@RequestBody CreateColabRequest request,
                                            @AuthenticationPrincipal Jwt jwt){
        String id = colabService.createColab(request, jwt.getSubject());
        return ResponseEntity.created(URI.create("/v1/colab/" + id)).build();
    }

    @PutMapping(path = "/support/{colabId}")
    public ResponseEntity<Void> supportColab(@PathVariable String colabId,
                                             @AuthenticationPrincipal Jwt jwt) {
        supportService.updateSupport(colabId, jwt.getSubject());
        return ResponseEntity.ok().build();
    }

    @GetMapping(path = "/{colabId}")
    public ResponseEntity<ColabResponse> getColab(@PathVariable String colabId) {
        ColabResponse colabResponse = colabService.getById(colabId);
        return ResponseEntity.ok(colabResponse);
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<UserResponse> getUser(@PathVariable String userId) {
        UserResponse userResponse = colabService.getColabsByUserId(userId);
        return ResponseEntity.ok(userResponse);
    }
}
