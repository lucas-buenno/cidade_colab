package tech.cidade.colab.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tech.cidade.colab.dto.response.CategoryResponse;
import tech.cidade.colab.service.CategoryService;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping(path = "/v1/categories")
public class CategoryController {


    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getCategories(@RequestParam(defaultValue = "false") boolean includeInactive) {
        log.info("Requisição recebida para buscar categorias. includeInactive: {}", includeInactive);
        List<CategoryResponse> categories = categoryService.getCategories(includeInactive);
        log.info("Categorias retornadas: {}", categories.size());
        return ResponseEntity.ok(categories);

    }
}
