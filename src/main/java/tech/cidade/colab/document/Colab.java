package tech.cidade.colab.document;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import tech.cidade.colab.dto.Location;
import tech.cidade.colab.enums.EColabStatus;

import java.time.Instant;
import java.util.List;

@Document(collection = "colabs")
@Data
@Accessors(chain = true)
public class Colab {

    @Id
    private String id;
    @Indexed(unique = false)
    private String userId;
    private String title;
    private String description;
    private List<String> categories;
    private Integer supportCount;
    private Location location;
    private EColabStatus status;
    private Instant createdAt;
    private Instant updatedAt;
}
