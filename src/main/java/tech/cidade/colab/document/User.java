package tech.cidade.colab.document;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "colab_users")
@Data
@Accessors(chain = true)
public class User {

    @Id
    private String id;
    @Indexed(unique = true)
    private String username;
    @Indexed(unique = true)
    private String email;
    private Instant createdAt;
    private Instant updatedAt;

    public static User create(String id, String username, String email) {
        return new User()
                .setId(id)
                .setUsername(username)
                .setEmail(email)
                .setCreatedAt(Instant.now())
                .setUpdatedAt(Instant.now());
    }
}
