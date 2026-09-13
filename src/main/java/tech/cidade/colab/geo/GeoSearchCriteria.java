package tech.cidade.colab.geo;

import java.util.List;

public record GeoSearchCriteria(
        SearchMode mode,
        Double nearLng,
        Double nearLat,
        Double maxDistanceMeters,
        BoundingBox bbox,
        List<String> categorySlugs,
        String textQuery
) {
    public boolean isGeo() {
        return mode != SearchMode.FILTER;
    }
}
