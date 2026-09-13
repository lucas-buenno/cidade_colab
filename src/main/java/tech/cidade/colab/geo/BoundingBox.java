package tech.cidade.colab.geo;

public record BoundingBox(double minLng, double minLat, double maxLng, double maxLat) {

    public static final double MAX_DIAGONAL_KM = 15.0;

    public static BoundingBox parse(String bbox) {
        if (bbox == null || bbox.isBlank()) {
            return null;
        }

        String[] parts = bbox.split(",");
        if (parts.length != 4) {
            throw new IllegalArgumentException(
                    "bbox malformado. Use minLng,minLat,maxLng,maxLat (GeoJSON, WGS84)"
            );
        }

        try {
            double minLng = Double.parseDouble(parts[0].trim());
            double minLat = Double.parseDouble(parts[1].trim());
            double maxLng = Double.parseDouble(parts[2].trim());
            double maxLat = Double.parseDouble(parts[3].trim());
            return of(minLng, minLat, maxLng, maxLat);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(
                    "bbox malformado. Use minLng,minLat,maxLng,maxLat (GeoJSON, WGS84)"
            );
        }
    }

    public static BoundingBox of(double minLng, double minLat, double maxLng, double maxLat) {
        GeoJsonCoordinates.validateLngLat(minLng, minLat);
        GeoJsonCoordinates.validateLngLat(maxLng, maxLat);
        if (minLng >= maxLng || minLat >= maxLat) {
            throw new IllegalArgumentException(
                    "bbox inválido: minLng deve ser menor que maxLng e minLat menor que maxLat"
            );
        }

        double diagonalMeters = GeoJsonCoordinates.haversineMeters(minLng, minLat, maxLng, maxLat);
        if (diagonalMeters > MAX_DIAGONAL_KM * 1000) {
            throw new IllegalArgumentException(
                    "bbox muito grande: a diagonal deve ser de no máximo " + (int) MAX_DIAGONAL_KM + " km"
            );
        }
        return new BoundingBox(minLng, minLat, maxLng, maxLat);
    }

    public double centerLng() {
        return (minLng + maxLng) / 2.0;
    }

    public double centerLat() {
        return (minLat + maxLat) / 2.0;
    }

    public double halfDiagonalMeters() {
        return GeoJsonCoordinates.haversineMeters(minLng, minLat, maxLng, maxLat) / 2.0;
    }
}
