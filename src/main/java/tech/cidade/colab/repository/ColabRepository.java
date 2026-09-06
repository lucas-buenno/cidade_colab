package tech.cidade.colab.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import tech.cidade.colab.document.Colab;

import java.util.List;

public interface ColabRepository extends CrudRepository<Colab, String> {
    List<Colab> findAllByOrderByIdDesc(Pageable pageable);

    List<Colab> findByIdLessThanOrderByIdDesc(String id, Pageable pageable);
}
