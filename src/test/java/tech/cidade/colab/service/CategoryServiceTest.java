package tech.cidade.colab.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.cidade.colab.document.Category;
import tech.cidade.colab.repository.CategoryRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    void requireActiveSlugsAcceptsActiveCategory() {
        when(categoryRepository.findAllById(List.of("pothole"))).thenReturn(List.of(
                new Category().setSlug("pothole").setActive(true)
        ));
        assertDoesNotThrow(() -> categoryService.requireActiveSlugs(List.of("pothole")));
    }

    @Test
    void requireActiveSlugsRejectsUnknownSlug() {
        when(categoryRepository.findAllById(List.of("missing"))).thenReturn(List.of());
        assertThrows(IllegalArgumentException.class, () -> categoryService.requireActiveSlugs(List.of("missing")));
    }

    @Test
    void requireActiveSlugsRejectsInactiveSlug() {
        when(categoryRepository.findAllById(List.of("pothole"))).thenReturn(List.of(
                new Category().setSlug("pothole").setActive(false)
        ));
        assertThrows(IllegalArgumentException.class, () -> categoryService.requireActiveSlugs(List.of("pothole")));
    }
}
