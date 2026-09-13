package tech.cidade.colab.repository;

import org.bson.Document;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import tech.cidade.colab.document.Colab;
import tech.cidade.colab.document.ColabDistanceHit;
import tech.cidade.colab.enums.EColabStatus;
import tech.cidade.colab.geo.BoundingBox;
import tech.cidade.colab.geo.ColabSearchPageCursor;
import tech.cidade.colab.geo.FilterSearchCursor;
import tech.cidade.colab.geo.GeoSearchCriteria;
import tech.cidade.colab.geo.GeoSearchCursor;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Repository
public class ColabGeoSearchRepository {

    private static final String COLLECTION = "colabs";
    private static final String DISTANCE_FIELD = "distanceMeters";

    private final MongoTemplate mongoTemplate;

    public ColabGeoSearchRepository(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public List<ColabDistanceHit> search(GeoSearchCriteria criteria, ColabSearchPageCursor cursor, int limit) {
        if (criteria.isGeo()) {
            if (cursor != null && !(cursor instanceof GeoSearchCursor)) {
                throw new IllegalArgumentException("pageToken inválido ou corrompido");
            }
            return searchGeo(criteria, (GeoSearchCursor) cursor, limit);
        }
        if (cursor != null && !(cursor instanceof FilterSearchCursor)) {
            throw new IllegalArgumentException("pageToken inválido ou corrompido");
        }
        return searchFilter(criteria, (FilterSearchCursor) cursor, limit);
    }

    private List<ColabDistanceHit> searchGeo(GeoSearchCriteria criteria, GeoSearchCursor cursor, int limit) {
        List<AggregationOperation> operations = new ArrayList<>();
        operations.add(context -> geoNearStage(criteria));
        operations.add(context -> new Document("$addFields",
                new Document(DISTANCE_FIELD, new Document("$round", List.of("$" + DISTANCE_FIELD, 0)))));

        if (cursor != null) {
            operations.add(Aggregation.match(new Criteria().orOperator(
                    Criteria.where(DISTANCE_FIELD).gt(cursor.distanceMeters()),
                    Criteria.where(DISTANCE_FIELD).is((double) cursor.distanceMeters())
                            .and("_id").gt(cursor.id())
            )));
        }

        operations.add(Aggregation.sort(Sort.by(
                Sort.Order.asc(DISTANCE_FIELD),
                Sort.Order.asc("_id")
        )));
        operations.add(Aggregation.limit(limit));

        AggregationResults<Document> results = mongoTemplate.aggregate(
                Aggregation.newAggregation(operations),
                COLLECTION,
                Document.class
        );

        List<ColabDistanceHit> hits = new ArrayList<>();
        for (Document document : results.getMappedResults()) {
            Number distance = document.get(DISTANCE_FIELD, Number.class);
            document.remove(DISTANCE_FIELD);
            Colab colab = mongoTemplate.getConverter().read(Colab.class, document);
            Long distanceMeters = distance == null ? null : Math.round(distance.doubleValue());
            hits.add(new ColabDistanceHit(colab, distanceMeters));
        }
        return hits;
    }

    private List<ColabDistanceHit> searchFilter(GeoSearchCriteria criteria, FilterSearchCursor cursor, int limit) {
        Query query = new Query(buildMatchCriteria(criteria));
        if (cursor != null) {
            query.addCriteria(new Criteria().orOperator(
                    Criteria.where("createdAt").lt(cursor.createdAt()),
                    Criteria.where("createdAt").is(cursor.createdAt()).and("_id").lt(cursor.id())
            ));
        }
        query.with(Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("_id")));
        query.limit(limit);

        return mongoTemplate.find(query, Colab.class, COLLECTION).stream()
                .map(colab -> new ColabDistanceHit(colab, null))
                .toList();
    }

    private Document geoNearStage(GeoSearchCriteria criteria) {
        Document geoNear = new Document();
        geoNear.append("near", new Document("type", "Point")
                .append("coordinates", List.of(criteria.nearLng(), criteria.nearLat())));
        geoNear.append("distanceField", DISTANCE_FIELD);
        geoNear.append("spherical", true);
        geoNear.append("key", "location");
        geoNear.append("maxDistance", criteria.maxDistanceMeters());
        geoNear.append("query", buildGeoQuery(criteria));
        return new Document("$geoNear", geoNear);
    }

    private Document buildGeoQuery(GeoSearchCriteria criteria) {
        Document query = buildMatchCriteria(criteria).getCriteriaObject();
        if (criteria.bbox() != null) {
            query.put("location", geoWithinGeometry(criteria.bbox()));
        }
        return query;
    }

    private Criteria buildMatchCriteria(GeoSearchCriteria criteria) {
        List<Criteria> filters = new ArrayList<>();
        filters.add(Criteria.where("status").is(EColabStatus.CREATED));
        filters.add(new Criteria().orOperator(
                Criteria.where("deleted").exists(false),
                Criteria.where("deleted").is(false)
        ));

        if (criteria.categorySlugs() != null && !criteria.categorySlugs().isEmpty()) {
            filters.add(Criteria.where("categories").in(criteria.categorySlugs()));
        }

        if (criteria.textQuery() != null) {
            String escaped = Pattern.quote(criteria.textQuery());
            filters.add(new Criteria().orOperator(
                    Criteria.where("title").regex(escaped, "i"),
                    Criteria.where("description").regex(escaped, "i"),
                    Criteria.where("location.name").regex(escaped, "i"),
                    Criteria.where("location.address.neighborhood").regex(escaped, "i"),
                    Criteria.where("location.address.city").regex(escaped, "i"),
                    Criteria.where("location.address.street").regex(escaped, "i")
            ));
        }

        return new Criteria().andOperator(filters.toArray(Criteria[]::new));
    }

    private Document geoWithinGeometry(BoundingBox bbox) {
        List<List<Double>> ring = List.of(
                List.of(bbox.minLng(), bbox.minLat()),
                List.of(bbox.maxLng(), bbox.minLat()),
                List.of(bbox.maxLng(), bbox.maxLat()),
                List.of(bbox.minLng(), bbox.maxLat()),
                List.of(bbox.minLng(), bbox.minLat())
        );
        Document geometry = new Document("type", "Polygon")
                .append("coordinates", List.of(ring));
        return new Document("$geoWithin", new Document("$geometry", geometry));
    }
}
