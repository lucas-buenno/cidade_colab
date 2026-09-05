package tech.cidade.colab.dto.response;

import tech.cidade.colab.document.Category;
import tech.cidade.colab.enums.ECategoryGroup;

import java.util.List;

public record CategoryResponse(
        String slug,
        String name,
        String description,
        ECategoryGroup group,
        List<String> keywords) {

    public static CategoryResponse from(Category category) {
        return new CategoryResponse(
                category.getSlug(),
                category.getName(),
                category.getDescription(),
                category.getGroup(),
                category.getKeywords()
        );
    }
}
