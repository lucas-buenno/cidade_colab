package tech.cidade.colab.repository;

import org.springframework.data.repository.CrudRepository;
import tech.cidade.colab.document.Colab;

public interface ColabRepository extends CrudRepository<Colab, String> {
}
