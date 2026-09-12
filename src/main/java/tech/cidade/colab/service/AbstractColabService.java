package tech.cidade.colab.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tech.cidade.colab.document.Colab;
import tech.cidade.colab.dto.response.CategoryResponse;
import tech.cidade.colab.dto.response.ColabResponse;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractColabService {

    protected final CloudinaryService cloudinaryService;
    protected final UserService userService;
    protected final CategoryService categoryService;

    protected ColabResponse generateColabResponse(Colab colab) {
        String username = userService.getUserById(colab.getUserId()).getUsername();
        String imageUrl = cloudinaryService.getImageUrl(colab.getImageKey());
        return new ColabResponse(
                colab.getId(),
                colab.getUserId(),
                username,
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

    protected List<CategoryResponse> mapCategories(List<String> slugs) {
        log.info("Mapeando categorias para slugs: {}", slugs);
        List<CategoryResponse> categories = categoryService.getCategoriesById(slugs).stream()
                .map(CategoryResponse::from)
                .toList();
        log.info("Categorias mapeadas: {}", categories);
        return categories;
    }
}
