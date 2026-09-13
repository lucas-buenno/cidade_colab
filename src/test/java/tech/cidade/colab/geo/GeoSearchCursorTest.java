package tech.cidade.colab.geo;

import org.junit.jupiter.api.Test;
import tech.cidade.colab.utils.CursorUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GeoSearchCursorTest {

    @Test
    void roundTripsDistanceAndId() {
        String token = GeoSearchCursor.encode(312, "colab-1");
        GeoSearchCursor cursor = GeoSearchCursor.decode(token);
        assertEquals(312, cursor.distanceMeters());
        assertEquals("colab-1", cursor.id());
    }

    @Test
    void blankTokenIsAbsent() {
        assertNull(GeoSearchCursor.decode(" "));
        assertNull(GeoSearchCursor.decode(null));
    }

    @Test
    void rejectsFilterToken() {
        String filterToken = FilterSearchCursor.encode(java.time.Instant.parse("2026-01-01T00:00:00Z"), "colab-1");
        assertThrows(IllegalArgumentException.class, () -> GeoSearchCursor.decode(filterToken));
    }

    @Test
    void rejectsFeedObjectIdToken() {
        String feedToken = CursorUtils.encode("64b7f2c8a1d4e5f678901234");
        assertThrows(IllegalArgumentException.class, () -> GeoSearchCursor.decode(feedToken));
    }

    @Test
    void rejectsGarbage() {
        assertThrows(IllegalArgumentException.class, () -> GeoSearchCursor.decode("not-a-token"));
    }
}
