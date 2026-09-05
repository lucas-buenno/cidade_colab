package tech.cidade.colab.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tech.cidade.colab.document.Colab;
import tech.cidade.colab.dto.response.CategoryResponse;
import tech.cidade.colab.dto.response.ColabResponse;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeedService {


    private final ColabService colabService;
    private final CategoryService categoryService;

    public List<ColabResponse> getFeed() {
        log.info("Buscando feed de colabs");
        List<Colab> colabs = colabService.findAllColabs();

        List<ColabResponse> colabResponses = colabs.stream()
                .map(colab -> new ColabResponse(
                        colab.getId(),
                        colab.getUserId(),
                        colab.getTitle(),
                        colab.getDescription(),
                        mapCategories(colab.getCategories()),
                        colab.getStatus(),
                        colab.getSupportCount(),
                        colab.getLocation(),
                        colab.getCreatedAt(),
                        colab.getUpdatedAt()
                ))
                .toList();

        log.info("Feed de colabs mapeado: Itens: {}", colabResponses.size());
        return colabResponses;
    }

    private List<CategoryResponse> mapCategories(List<String> slugs) {
        log.info("Mapeando categorias para slugs: {}", slugs);
        List<CategoryResponse> categories = categoryService.getCategoriesById(slugs).stream()
                .map(CategoryResponse::from)
                .toList();
        log.info("Categorias mapeadas: {}", categories);
        return categories;
    }



}
