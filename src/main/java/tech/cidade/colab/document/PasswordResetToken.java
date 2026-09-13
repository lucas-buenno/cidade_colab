package tech.cidade.colab.document;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "password_reset_tokens")
@Data
@Accessors(chain = true)
public class PasswordResetToken {

    @Id
    private String id;
    @Indexed(unique = true)
    private String tokenHash;
    @Indexed
    private String userId;
    @Indexed(expireAfterSeconds = 0)
    private Instant expiresAt;
    private Instant usedAt;
    private Instant createdAt;
}
