package tech.cidade.colab.repository;

import org.springframework.data.repository.CrudRepository;
import tech.cidade.colab.document.ColabSupport;

public interface ColabSupportRepository extends CrudRepository<ColabSupport, String> {
    boolean existsByColabIdAndUserId(String colabId, String userId);
}
