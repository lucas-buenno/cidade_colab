package tech.cidade.colab.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import tech.cidade.colab.document.Colab;

import java.util.List;

public interface ColabRepository extends CrudRepository<Colab, String> {
    List<Colab> findAllByOrderByCreatedAtDesc(Pageable pageable);

    List<Colab> findByIdLessThanOrderByCreatedAtDesc(String id, Pageable pageable);

    List<Colab> findByUserIdOrderByCreatedAtDesc(String userId);
}
