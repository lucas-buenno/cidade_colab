package tech.cidade.colab.dto;


import tech.cidade.colab.dto.request.LocationRequest;
import tech.cidade.colab.geo.GeoJsonCoordinates;

public record Location(String name,
                       String reference,
                       String type,
                       Address address,
                       double[] coordinates) {

    public static Location from(LocationRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("location é obrigatório");
        }
        Address address = Address.from(request.street(), request.number(), request.neighborhood(), request.postalCode());
        return new Location(
                request.name(),
                request.reference(),
                GeoJsonCoordinates.POINT_TYPE,
                address,
                GeoJsonCoordinates.normalizeToLngLat(request.coordinates())
        );
    }

    public double longitude() {
        return coordinates[0];
    }

    public double latitude() {
        return coordinates[1];
    }
}
