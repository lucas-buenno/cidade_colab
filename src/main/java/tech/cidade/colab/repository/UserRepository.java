package tech.cidade.colab.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import tech.cidade.colab.document.User;

@Repository
public interface UserRepository extends CrudRepository<User, String> {

    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
