package tech.cidade.colab.geo;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GeoSearchQueryFactoryTest {

    @Test
    void usesDefaultRadiusForPointSearch() {
        GeoSearchCriteria criteria = GeoSearchQueryFactory.create(-20.14, -44.88, null, null, null, null);
        assertEquals(SearchMode.NEAR, criteria.mode());
        assertEquals(-44.88, criteria.nearLng(), 0.0001);
        assertEquals(-20.14, criteria.nearLat(), 0.0001);
        assertEquals(3000.0, criteria.maxDistanceMeters(), 0.001);
        assertNull(criteria.bbox());
        assertTrue(criteria.isGeo());
    }

    @Test
    void ignoresLatLngWhenBboxIsValid() {
        GeoSearchCriteria criteria = GeoSearchQueryFactory.create(
                -10.0,
                -20.0,
                99.0,
                "-44.90,-20.16,-44.86,-20.12",
                List.of("pothole"),
                "buraco"
        );
        assertEquals(SearchMode.BBOX, criteria.mode());
        assertEquals(-44.88, criteria.nearLng(), 0.001);
        assertEquals(-20.14, criteria.nearLat(), 0.001);
        assertTrue(criteria.maxDistanceMeters() > 0);
        assertEquals("buraco", criteria.textQuery());
        assertEquals(List.of("pothole"), criteria.categorySlugs());
    }

    @Test
    void allowsTextOnlySearch() {
        GeoSearchCriteria criteria = GeoSearchQueryFactory.create(null, null, null, null, null, "Centro");
        assertEquals(SearchMode.FILTER, criteria.mode());
        assertFalse(criteria.isGeo());
        assertEquals("Centro", criteria.textQuery());
        assertTrue(criteria.categorySlugs().isEmpty());
        assertNull(criteria.nearLng());
    }

    @Test
    void allowsCategoryOnlySearch() {
        GeoSearchCriteria criteria = GeoSearchQueryFactory.create(
                null, null, null, null, List.of("pothole", "flooding"), null
        );
        assertEquals(SearchMode.FILTER, criteria.mode());
        assertEquals(List.of("pothole", "flooding"), criteria.categorySlugs());
        assertNull(criteria.textQuery());
    }

    @Test
    void rejectsEmptySearch() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> GeoSearchQueryFactory.create(null, null, null, null, null, null)
        );
        assertTrue(exception.getMessage().contains("category") || exception.getMessage().contains("q"));
    }

    @Test
    void rejectsIncompleteLatLng() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> GeoSearchQueryFactory.create(-20.14, null, 3.0, null, null, null)
        );
        assertTrue(exception.getMessage().contains("juntos"));
    }

    @Test
    void rejectsRadiusOutOfRange() {
        IllegalArgumentException tooSmall = assertThrows(
                IllegalArgumentException.class,
                () -> GeoSearchQueryFactory.create(-20.14, -44.88, 0.1, null, null, null)
        );
        IllegalArgumentException tooLarge = assertThrows(
                IllegalArgumentException.class,
                () -> GeoSearchQueryFactory.create(-20.14, -44.88, 10.1, null, null, null)
        );
        assertTrue(tooSmall.getMessage().contains("radiusKm"));
        assertTrue(tooLarge.getMessage().contains("radiusKm"));
    }
}
