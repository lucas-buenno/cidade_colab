package tech.cidade.colab.geo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GeoJsonCoordinatesTest {

    @Test
    void keepsGeoJsonLngLat() {
        double[] normalized = GeoJsonCoordinates.normalizeToLngLat(-46.63, -23.55);
        assertArrayEquals(new double[]{-46.63, -23.55}, normalized, 0.0001);
    }

    @Test
    void swapsHistoricalLatLngExample() {
        double[] normalized = GeoJsonCoordinates.normalizeToLngLat(-23.55, -46.63);
        assertArrayEquals(new double[]{-46.63, -23.55}, normalized, 0.0001);
    }

    @Test
    void swapsDivinopolisLatLng() {
        double[] normalized = GeoJsonCoordinates.normalizeToLngLat(-20.14, -44.88);
        assertArrayEquals(new double[]{-44.88, -20.14}, normalized, 0.0001);
    }

    @Test
    void rejectsInvalidLength() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> GeoJsonCoordinates.normalizeToLngLat(new double[]{-46.63})
        );
        assertTrue(exception.getMessage().contains("GeoJSON"));
    }

    @Test
    void haversineIsSymmetricAndPositive() {
        double meters = GeoJsonCoordinates.haversineMeters(-44.88, -20.14, -44.87, -20.14);
        assertTrue(meters > 900 && meters < 1200);
        assertEquals(meters, GeoJsonCoordinates.haversineMeters(-44.87, -20.14, -44.88, -20.14), 0.001);
    }
}
