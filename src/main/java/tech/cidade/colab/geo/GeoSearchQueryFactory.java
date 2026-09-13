package tech.cidade.colab.geo;

import java.util.List;

public final class GeoSearchQueryFactory {

    public static final double DEFAULT_RADIUS_KM = 3.0;
    public static final double MIN_RADIUS_KM = 0.2;
    public static final double MAX_RADIUS_KM = 10.0;

    private GeoSearchQueryFactory() {
    }

    public static GeoSearchCriteria create(
            Double lat,
            Double lng,
            Double radiusKm,
            String bboxRaw,
            List<String> categorySlugs,
            String q
    ) {
        String textQuery = (q == null || q.isBlank()) ? null : q.trim();
        if (textQuery != null && textQuery.length() > 200) {
            throw new IllegalArgumentException("q deve ter no máximo 200 caracteres");
        }

        List<String> slugs = categorySlugs == null
                ? List.of()
                : categorySlugs.stream().filter(slug -> slug != null && !slug.isBlank()).distinct().toList();

        BoundingBox bbox = BoundingBox.parse(bboxRaw);
        if (bbox != null) {
            return new GeoSearchCriteria(
                    SearchMode.BBOX,
                    bbox.centerLng(),
                    bbox.centerLat(),
                    Math.max(1.0, bbox.halfDiagonalMeters()),
                    bbox,
                    slugs,
                    textQuery
            );
        }

        if (lat != null || lng != null) {
            if (lat == null || lng == null) {
                throw new IllegalArgumentException("lat e lng devem ser enviados juntos");
            }
            GeoJsonCoordinates.validateLngLat(lng, lat);

            double effectiveRadiusKm = radiusKm == null ? DEFAULT_RADIUS_KM : radiusKm;
            if (effectiveRadiusKm < MIN_RADIUS_KM || effectiveRadiusKm > MAX_RADIUS_KM) {
                throw new IllegalArgumentException(
                        "radiusKm deve estar entre " + MIN_RADIUS_KM + " e " + MAX_RADIUS_KM
                );
            }

            return new GeoSearchCriteria(
                    SearchMode.NEAR,
                    lng,
                    lat,
                    effectiveRadiusKm * 1000.0,
                    null,
                    slugs,
                    textQuery
            );
        }

        if (textQuery == null && slugs.isEmpty()) {
            throw new IllegalArgumentException(
                    "Informe lat e lng, um bbox, ao menos uma category, ou q"
            );
        }

        return new GeoSearchCriteria(SearchMode.FILTER, null, null, null, null, slugs, textQuery);
    }
}
