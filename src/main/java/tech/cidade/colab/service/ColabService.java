package tech.cidade.colab.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import tech.cidade.colab.document.Colab;
import tech.cidade.colab.dto.Location;
import tech.cidade.colab.dto.request.CreateColabRequest;
import tech.cidade.colab.dto.response.CategoryResponse;
import tech.cidade.colab.dto.response.ColabResponse;
import tech.cidade.colab.enums.EColabStatus;
import tech.cidade.colab.repository.ColabRepository;


import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ColabService {

    private final ColabRepository colabRepository;
    private final CategoryService categoryService;
    private final SupportService supportService;
    private final CloudinaryService cloudinaryService;

    public String createColab(CreateColabRequest request, String authenticatedUserId) {
        log.info("Criando colab com os dados: {}", request);
        Colab colabToSave = new Colab()
                .setId(request.colabId())
                .setUserId(authenticatedUserId)
                .setTitle(request.title())
                .setDescription(request.description())
                .setCategories(request.categoriesSlugs())
                .setSupportCount(0)
                .setCreatedAt(Instant.now())
                .setUpdatedAt(Instant.now())
                .setStatus(EColabStatus.CREATED)
                .setLocation(Location.from(request.location()))
                .setImageKey(request.imageKey());

        Colab created = colabRepository.save(colabToSave);
        log.info("Colab criado com sucesso: {} - Criado em: {}", created, created.getCreatedAt());
        supportService.updateSupport(created.getId(), authenticatedUserId);
        return created.getId();
    }

    public ColabResponse getById(String colabId) {
        log.info("Buscando colab com id: {}", colabId);
        Colab colab = colabRepository.findById(colabId).orElseThrow(() -> new RuntimeException("Colab não encontrado com id: " + colabId));
        log.info("Colab encontrado: {}", colab);

        ColabResponse response = generateColabResponse(colab);
        log.info("ColabResponse mapeado: {}", response);
        return response;
    }

    public List<Colab> findRecentColabs(int limit) {
        return colabRepository.findAllByOrderByIdDesc(PageRequest.of(0, limit));
    }

    public List<Colab> findRecentColabsBeforeId(String id, int limit) {
        return colabRepository.findByIdLessThanOrderByIdDesc(id, PageRequest.of(0, limit));
    }

    private  ColabResponse generateColabResponse(Colab colab) {

        String imageUrl = cloudinaryService.getImageUrl(colab.getImageKey());
        return new ColabResponse(
                colab.getId(),
                colab.getUserId(),
                colab.getTitle(),
                colab.getDescription(),
                mapCategories(colab.getCategories()),
                colab.getStatus(),
                colab.getSupportCount(),
                colab.getLocation(),
                colab.getCreatedAt(),
                colab.getUpdatedAt(),
                imageUrl
        );
    }

    private List<CategoryResponse> mapCategories(List<String> slugs) {
        log.info("Mapeando categorias para slugs: {}", slugs);
        List<CategoryResponse> categories = categoryService.getCategoriesById(slugs).stream()
                .map(CategoryResponse::from)
                .toList();
        log.info("Categorias mapeadas: {}", categories);
        return categories;
    }

    public List<Colab> findAllColabs() {
        return (List<Colab>) colabRepository.findAll();
    }

}
