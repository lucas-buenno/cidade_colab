package tech.cidade.colab.geo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BoundingBoxTest {

    @Test
    void parsesValidBbox() {
        BoundingBox bbox = BoundingBox.parse("-44.90,-20.16,-44.86,-20.12");
        assertEquals(-44.90, bbox.minLng(), 0.0001);
        assertEquals(-20.16, bbox.minLat(), 0.0001);
        assertEquals(-44.86, bbox.maxLng(), 0.0001);
        assertEquals(-20.12, bbox.maxLat(), 0.0001);
    }

    @Test
    void blankBboxIsAbsent() {
        assertNull(BoundingBox.parse(" "));
        assertNull(BoundingBox.parse(null));
    }

    @Test
    void rejectsMalformedBbox() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> BoundingBox.parse("-44.90,-20.16,-44.86")
        );
        assertTrue(exception.getMessage().contains("bbox malformado"));
    }

    @Test
    void rejectsInvertedBounds() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> BoundingBox.parse("-44.86,-20.16,-44.90,-20.12")
        );
        assertTrue(exception.getMessage().contains("bbox inválido"));
    }

    @Test
    void rejectsDiagonalGreaterThan15Km() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> BoundingBox.parse("-44.88,-20.14,-44.70,-19.95")
        );
        assertTrue(exception.getMessage().contains("bbox muito grande"));
    }
}
