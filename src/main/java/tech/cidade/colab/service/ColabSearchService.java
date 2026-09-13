package tech.cidade.colab.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tech.cidade.colab.document.Colab;
import tech.cidade.colab.document.ColabDistanceHit;
import tech.cidade.colab.dto.response.ColabSearchItemResponse;
import tech.cidade.colab.dto.response.ColabSearchPageResponse;
import tech.cidade.colab.geo.ColabSearchPageCursor;
import tech.cidade.colab.geo.FilterSearchCursor;
import tech.cidade.colab.geo.GeoSearchCriteria;
import tech.cidade.colab.geo.GeoSearchCursor;
import tech.cidade.colab.geo.GeoSearchQueryFactory;
import tech.cidade.colab.repository.ColabGeoSearchRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static tech.cidade.colab.utils.CursorUtils.validateAndNormalizeSize;

@Service
@Slf4j
public class ColabSearchService extends AbstractColabService {

    private final ColabGeoSearchRepository colabGeoSearchRepository;
    private final SupportService supportService;

    public ColabSearchService(
            CloudinaryService cloudinaryService,
            UserService userService,
            CategoryService categoryService,
            ColabGeoSearchRepository colabGeoSearchRepository,
            SupportService supportService
    ) {
        super(cloudinaryService, userService, categoryService);
        this.colabGeoSearchRepository = colabGeoSearchRepository;
        this.supportService = supportService;
    }

    public ColabSearchPageResponse search(
            Double lat,
            Double lng,
            Double radiusKm,
            String bbox,
            List<String> category,
            String q,
            String pageToken,
            int size,
            String authenticatedUserId
    ) {
        int safeSize = validateAndNormalizeSize(size);
        GeoSearchCriteria criteria = GeoSearchQueryFactory.create(lat, lng, radiusKm, bbox, category, q);
        categoryService.requireActiveSlugs(criteria.categorySlugs());
        ColabSearchPageCursor cursor = decodeCursor(criteria, pageToken);

        log.info("Busca de colabs - mode={} near=[{}, {}] maxDistance={}m bbox={} q={} categories={} cursor={}",
                criteria.mode(), criteria.nearLng(), criteria.nearLat(), criteria.maxDistanceMeters(),
                criteria.bbox(), criteria.textQuery(), criteria.categorySlugs(), pageToken);

        List<ColabDistanceHit> hits = colabGeoSearchRepository.search(criteria, cursor, safeSize + 1);
        boolean hasNextPage = hits.size() > safeSize;
        List<ColabDistanceHit> pageItems = hasNextPage ? new ArrayList<>(hits.subList(0, safeSize)) : hits;

        Set<String> supportedIds = supportService.findSupportedColabIds(
                authenticatedUserId,
                pageItems.stream().map(hit -> hit.colab().getId()).toList()
        );

        List<ColabSearchItemResponse> items = pageItems.stream()
                .map(hit -> toItem(hit, supportedIds.contains(hit.colab().getId())))
                .toList();

        String nextPageToken = null;
        if (hasNextPage && !pageItems.isEmpty()) {
            nextPageToken = encodeNextToken(criteria, pageItems.get(pageItems.size() - 1));
        }

        log.info("Busca de colabs mapeada: itens={} hasNextPage={}", items.size(), hasNextPage);
        return new ColabSearchPageResponse(items, nextPageToken);
    }

    private ColabSearchPageCursor decodeCursor(GeoSearchCriteria criteria, String pageToken) {
        if (criteria.isGeo()) {
            return GeoSearchCursor.decode(pageToken);
        }
        return FilterSearchCursor.decode(pageToken);
    }

    private String encodeNextToken(GeoSearchCriteria criteria, ColabDistanceHit last) {
        if (criteria.isGeo()) {
            Long distance = last.distanceMeters();
            if (distance == null) {
                throw new IllegalArgumentException("Não foi possível gerar o cursor: distância inválida");
            }
            return GeoSearchCursor.encode(distance, last.colab().getId());
        }
        return FilterSearchCursor.encode(last.colab().getCreatedAt(), last.colab().getId());
    }

    private ColabSearchItemResponse toItem(ColabDistanceHit hit, boolean supportedByMe) {
        Colab colab = hit.colab();
        return new ColabSearchItemResponse(generateColabResponse(colab, supportedByMe), hit.distanceMeters());
    }
}
