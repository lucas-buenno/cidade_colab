package tech.cidade.colab.dto;

import org.junit.jupiter.api.Test;
import tech.cidade.colab.dto.request.LocationRequest;
import tech.cidade.colab.geo.GeoJsonCoordinates;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class LocationGeoJsonTest {

    @Test
    void createPersistsGeoJsonPointLngLat() {
        LocationRequest request = new LocationRequest(
                "Praça",
                "ao lado do banco",
                "POINT",
                "Rua A",
                "10",
                "Centro",
                "35500000",
                new double[]{-20.14, -44.88}
        );

        Location location = Location.from(request);

        assertEquals(GeoJsonCoordinates.POINT_TYPE, location.type());
        assertArrayEquals(new double[]{-44.88, -20.14}, location.coordinates(), 0.0001);
        assertEquals(-44.88, location.longitude(), 0.0001);
        assertEquals(-20.14, location.latitude(), 0.0001);
    }
}
