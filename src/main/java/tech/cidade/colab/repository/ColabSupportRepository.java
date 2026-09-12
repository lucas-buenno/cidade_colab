package tech.cidade.colab.repository;

import org.springframework.data.repository.CrudRepository;
import tech.cidade.colab.document.ColabSupport;

import java.util.Collection;
import java.util.List;

public interface ColabSupportRepository extends CrudRepository<ColabSupport, String> {
    boolean existsByColabIdAndUserId(String colabId, String userId);

    List<ColabSupport> findByUserIdAndColabIdIn(String userId, Collection<String> colabIds);
}
