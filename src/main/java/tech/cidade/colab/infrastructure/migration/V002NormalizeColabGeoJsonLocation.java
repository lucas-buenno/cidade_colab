package tech.cidade.colab.infrastructure.migration;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexType;
import org.springframework.data.mongodb.core.index.GeospatialIndex;
import org.springframework.data.mongodb.core.query.Query;
import tech.cidade.colab.geo.GeoJsonCoordinates;

import java.util.List;

@ChangeUnit(
        id = "v002-normalize-colab-geojson-location",
        order = "002",
        author = "admin"
)
public class V002NormalizeColabGeoJsonLocation {

    private static final String COLLECTION = "colabs";
    private static final String INDEX_NAME = "location_2dsphere";

    private final MongoTemplate mongoTemplate;

    public V002NormalizeColabGeoJsonLocation(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Execution
    public void execute() {
        List<Document> colabs = mongoTemplate.find(new Query(), Document.class, COLLECTION);
        for (Document colab : colabs) {
            Document location = colab.get("location", Document.class);
            if (location == null) {
                continue;
            }

            location.put("type", GeoJsonCoordinates.POINT_TYPE);

            List<?> rawCoordinates = location.getList("coordinates", Object.class);
            if (rawCoordinates != null && rawCoordinates.size() == 2) {
                double first = toDouble(rawCoordinates.get(0));
                double second = toDouble(rawCoordinates.get(1));
                double[] normalized = GeoJsonCoordinates.normalizeToLngLat(first, second);
                location.put("coordinates", List.of(normalized[0], normalized[1]));
            }

            mongoTemplate.save(colab, COLLECTION);
        }

        mongoTemplate.indexOps(COLLECTION).ensureIndex(
                new GeospatialIndex("location")
                        .typed(GeoSpatialIndexType.GEO_2DSPHERE)
                        .named(INDEX_NAME)
        );
    }

    @RollbackExecution
    public void rollback() {
        mongoTemplate.indexOps(COLLECTION).dropIndex(INDEX_NAME);
    }

    private static double toDouble(Object value) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        throw new IllegalArgumentException("coordinates inválidas no documento existente");
    }
}
