package tech.cidade.colab.geo;

public final class GeoJsonCoordinates {

    public static final String POINT_TYPE = "Point";

    /**
     * Brazil-focused heuristic: historical payloads used [lat, lng] (e.g. -23.55, -46.63).
     * GeoJSON and this API always persist [longitude, latitude].
     */
    private static final double BRAZIL_LAT_MIN = -35.0;
    private static final double BRAZIL_LAT_MAX = 6.0;
    private static final double BRAZIL_LNG_MIN = -74.0;
    private static final double BRAZIL_LNG_MAX = -32.0;

    private static final double EARTH_RADIUS_METERS = 6_371_000.0;

    private GeoJsonCoordinates() {
    }

    public static double[] normalizeToLngLat(double[] coordinates) {
        if (coordinates == null || coordinates.length != 2) {
            throw new IllegalArgumentException("coordinates deve ser GeoJSON [longitude, latitude]");
        }
        return normalizeToLngLat(coordinates[0], coordinates[1]);
    }

    public static double[] normalizeToLngLat(double first, double second) {
        if (looksLikeLatLng(first, second)) {
            validateLngLat(second, first);
            return new double[]{second, first};
        }
        validateLngLat(first, second);
        return new double[]{first, second};
    }

    public static void validateLngLat(double lng, double lat) {
        if (lng < -180 || lng > 180 || lat < -90 || lat > 90) {
            throw new IllegalArgumentException(
                    "coordinates fora do intervalo WGS84. Esperado GeoJSON [longitude, latitude]"
            );
        }
    }

    public static boolean looksLikeLatLng(double first, double second) {
        return isLikelyBrazilLat(first) && isLikelyBrazilLng(second);
    }

    public static double haversineMeters(double lng1, double lat1, double lng2, double lat2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_METERS * c;
    }

    private static boolean isLikelyBrazilLat(double value) {
        return value >= BRAZIL_LAT_MIN && value <= BRAZIL_LAT_MAX;
    }

    private static boolean isLikelyBrazilLng(double value) {
        return value >= BRAZIL_LNG_MIN && value <= BRAZIL_LNG_MAX;
    }
}
