package tech.cidade.colab.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tech.cidade.colab.dto.response.UploadImageResponse;
import tech.cidade.colab.utils.FileUtils;
import tech.cidade.colab.utils.JwtUtils;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@Slf4j
public class CloudinaryService {

    private final Cloudinary cloudinary;

    @Value("${colab.folder.prefix}")
    private String colabFolderPrefix;

    public UploadImageResponse uploadFile(byte[] fileBytes, String fileName) {

        try {
            FileUtils.validateFile(fileBytes);
            String colabId = UUID.randomUUID().toString();
            String imageKey = generateColabImagePublicId(colabId);

            log.info("Iniciando upload do arquivo com imageKey: {}", imageKey);
            Map<String, Object> uploadResult = uploadImage(fileBytes, imageKey);

            String finalUrl = getFinalUrl(uploadResult);
            log.info("Upload concluído para o arquivo: {} {}", imageKey, finalUrl);
            return new UploadImageResponse(imageKey, colabId, finalUrl);

        } catch (Exception e) {
            log.error("Erro ao fazer upload do arquivo: {}", fileName, e);
            throw new RuntimeException("Erro ao fazer upload do arquivo", e);
        }
    }

    public String getImageUrl(String imageKey) {
            return cloudinary.url()
                    .secure(true)
                    .resourceType("image")
                    .type("upload")
                    .generate(imageKey);
    }

    private Map<String, Object> uploadImage(byte[] fileBytes, String imageKey) throws IOException {
        return cloudinary.uploader().upload(
                fileBytes,
                ObjectUtils.asMap(
                        "public_id", imageKey,
                        "resource_type", "image",
                        "type", "upload",
                        "access_mode", "public",
                        "overwrite", false,
                        "asset_folder", imageKey
                )
        );
    }

    private String getFinalUrl(Map<?, ?> uploadResult) {
        Object secureUrlObj = uploadResult.get("secure_url");
        Object urlObj = uploadResult.get("url");
        String secureUrl = secureUrlObj != null ? secureUrlObj.toString() : null;
        String url = urlObj != null ? urlObj.toString() : null;
        String finalUrl = (secureUrl != null && !secureUrl.isBlank()) ? secureUrl : url;

        if (finalUrl == null || finalUrl.isBlank()) {
            throw new IllegalStateException("Cloudinary não retornou URL da imagem");
        }

        return finalUrl;
    }

    private String generateColabImagePublicId(String colabId) {
        String userId = JwtUtils.getUserId();
        return String.format("%s/%s/%s", colabFolderPrefix, userId, colabId);
    }
}
