package tech.cidade.colab.dto;


import tech.cidade.colab.dto.request.LocationRequest;

public record Location(String name,
                       String reference,
                       String type,
                       Address address,
                       double[] coordinates) {

    public static Location from(LocationRequest request) {
        Address address = Address.from(request.street(), request.number(), request.neighborhood(), request.postalCode());
        return new Location(
                request.name(),
                request.reference(),
                "POINT",
                address,
                request.coordinates()
        );
    }
}
