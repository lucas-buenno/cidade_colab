package tech.cidade.colab.geo;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FilterSearchCursorTest {

    @Test
    void roundTripsCreatedAtAndId() {
        Instant createdAt = Instant.parse("2026-01-01T00:00:00Z");
        String token = FilterSearchCursor.encode(createdAt, "colab-1");
        FilterSearchCursor cursor = FilterSearchCursor.decode(token);
        assertEquals(createdAt, cursor.createdAt());
        assertEquals("colab-1", cursor.id());
    }

    @Test
    void blankTokenIsAbsent() {
        assertNull(FilterSearchCursor.decode(" "));
        assertNull(FilterSearchCursor.decode(null));
    }

    @Test
    void rejectsGeoToken() {
        String geoToken = GeoSearchCursor.encode(312, "colab-1");
        assertThrows(IllegalArgumentException.class, () -> FilterSearchCursor.decode(geoToken));
    }

    @Test
    void rejectsGarbage() {
        assertThrows(IllegalArgumentException.class, () -> FilterSearchCursor.decode("not-a-token"));
    }
}
