package tech.cidade.colab.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tech.cidade.colab.document.Category;
import tech.cidade.colab.dto.response.CategoryResponse;
import tech.cidade.colab.repository.CategoryRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<CategoryResponse> getCategories(boolean includeInactive) {
        log.info("Buscando todas as categorias ativas");
        List<Category> categories = (List<Category>) categoryRepository.findAll();
        log.info("Categorias totais encontradas: {}", categories.size());

        if (includeInactive) {
            return categories.stream().map(CategoryResponse::from).toList();
        }

        List<CategoryResponse> categoryResponses = categories.stream()
                .filter(Category::isActive)
                .map(CategoryResponse::from)
                .toList();

        log.info("Categorias ativas encontradas: {}", categoryResponses.size());
        return categoryResponses;
    }

    public List<Category> getCategoriesById(List<String> slugs) {
        log.info("Buscando categorias por slugs: {}", slugs);
        Iterable<Category> categories = categoryRepository.findAllById(slugs);
        log.info("Categorias encontradas: {}", categories);
        return (List<Category>) categories;
    }

    public void requireActiveSlugs(List<String> slugs) {
        if (slugs == null || slugs.isEmpty()) {
            return;
        }

        List<Category> found = getCategoriesById(slugs);
        for (String slug : slugs) {
            Category category = found.stream()
                    .filter(item -> slug.equals(item.getSlug()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Categoria inexistente: " + slug));
            if (!category.isActive()) {
                throw new IllegalArgumentException("Categoria inexistente: " + slug);
            }
        }
    }
}
