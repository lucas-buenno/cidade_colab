package tech.cidade.colab.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tech.cidade.colab.document.Category;
import tech.cidade.colab.repository.CategoryRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<Category> getCategoriesById(List<String> slugs) {
        log.info("Buscando categorias por slugs: {}", slugs);
        Iterable<Category> categories = categoryRepository.findAllById(slugs);
        log.info("Categorias encontradas: {}", categories);
        return (List<Category>) categories;
    }
}
