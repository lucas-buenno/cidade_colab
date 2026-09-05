package tech.cidade.colab.repository;

import org.springframework.data.repository.CrudRepository;
import tech.cidade.colab.document.Category;

public interface CategoryRepository extends CrudRepository<Category, String> {
}
