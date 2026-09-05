package tech.cidade.colab.document;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document("colab_supports")
@CompoundIndex(name = "uniq_colab_user", def = "{'colabId': 1, 'userId': 1}", unique = true)
@Data
@Accessors(chain = true)
public class ColabSupport {
    @Id
    private String id;
    private String colabId;
    private String userId;
    private Instant createdAt;
}