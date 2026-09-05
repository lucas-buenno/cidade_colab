package tech.cidade.colab.document;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import tech.cidade.colab.enums.ECategoryGroup;

import java.time.Instant;
import java.util.List;

@Data
@Accessors(chain = true)
@Document(collection = "colab_categories")
public class Category {
    @Id
    private String slug;
    private String name;
    private String description;
    @Indexed(unique = true)
    private ECategoryGroup group;
    private List<String> keywords;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;
}
