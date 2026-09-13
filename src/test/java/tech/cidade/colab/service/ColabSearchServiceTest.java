package tech.cidade.colab.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import tech.cidade.colab.document.Category;
import tech.cidade.colab.document.Colab;
import tech.cidade.colab.document.ColabDistanceHit;
import tech.cidade.colab.document.User;
import tech.cidade.colab.dto.Address;
import tech.cidade.colab.dto.Location;
import tech.cidade.colab.dto.response.ColabSearchPageResponse;
import tech.cidade.colab.enums.EColabStatus;
import tech.cidade.colab.geo.FilterSearchCursor;
import tech.cidade.colab.geo.GeoSearchCriteria;
import tech.cidade.colab.geo.GeoSearchCursor;
import tech.cidade.colab.repository.ColabGeoSearchRepository;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ColabSearchServiceTest {

    @Mock
    private CloudinaryService cloudinaryService;
    @Mock
    private UserService userService;
    @Mock
    private CategoryService categoryService;
    @Mock
    private ColabGeoSearchRepository colabGeoSearchRepository;
    @Mock
    private SupportService supportService;

    private ColabSearchService colabSearchService;

    @BeforeEach
    void setUp() {
        colabSearchService = new ColabSearchService(
                cloudinaryService,
                userService,
                categoryService,
                colabGeoSearchRepository,
                supportService
        );
    }

    @Test
    void pointSearchReturnsDistanceAndEmptyNextToken() {
        stubMapping();
        when(colabGeoSearchRepository.search(any(), isNull(), eq(21)))
                .thenReturn(List.of(new ColabDistanceHit(colab("a"), 312L)));
        when(supportService.findSupportedColabIds(isNull(), anyList())).thenReturn(Set.of());

        ColabSearchPageResponse response = colabSearchService.search(
                -20.14, -44.88, 3.0, null, null, null, null, 20, null
        );

        assertEquals(1, response.items().size());
        assertEquals(312, response.items().get(0).distanceMeters());
        assertEquals("a", response.items().get(0).colab().id());
        assertNull(response.nextPageToken());
        assertTrue(!response.items().get(0).colab().supportedByMe());

        ArgumentCaptor<GeoSearchCriteria> captor = ArgumentCaptor.forClass(GeoSearchCriteria.class);
        verify(colabGeoSearchRepository).search(captor.capture(), isNull(), eq(21));
        assertEquals(3000.0, captor.getValue().maxDistanceMeters(), 0.001);
        assertNull(captor.getValue().bbox());
    }

    @Test
    void bboxSearchIgnoresRadiusAndUsesBox() {
        stubMapping();
        when(colabGeoSearchRepository.search(any(), isNull(), anyInt()))
                .thenReturn(List.of());
        when(supportService.findSupportedColabIds(isNull(), anyList())).thenReturn(Set.of());

        ColabSearchPageResponse response = colabSearchService.search(
                -20.14, -44.88, 0.05, "-44.90,-20.16,-44.86,-20.12", null, null, null, 20, null
        );

        assertTrue(response.items().isEmpty());
        assertNull(response.nextPageToken());

        ArgumentCaptor<GeoSearchCriteria> captor = ArgumentCaptor.forClass(GeoSearchCriteria.class);
        verify(colabGeoSearchRepository).search(captor.capture(), isNull(), eq(21));
        assertTrue(captor.getValue().bbox() != null);
    }

    @Test
    void filtersByCategoryAndText() {
        stubMapping();
        when(colabGeoSearchRepository.search(any(), isNull(), anyInt()))
                .thenReturn(List.of(new ColabDistanceHit(colab("a"), 10L)));
        when(supportService.findSupportedColabIds(isNull(), anyList())).thenReturn(Set.of());

        colabSearchService.search(-20.14, -44.88, 3.0, null, List.of("pothole"), "buraco", null, 20, null);

        ArgumentCaptor<GeoSearchCriteria> captor = ArgumentCaptor.forClass(GeoSearchCriteria.class);
        verify(colabGeoSearchRepository).search(captor.capture(), isNull(), eq(21));
        verify(categoryService).requireActiveSlugs(List.of("pothole"));
        assertEquals(List.of("pothole"), captor.getValue().categorySlugs());
        assertEquals("buraco", captor.getValue().textQuery());
    }

    @Test
    void categorySearchWithoutGeoUsesFilterMode() {
        stubMapping();
        when(colabGeoSearchRepository.search(any(), isNull(), anyInt()))
                .thenReturn(List.of(new ColabDistanceHit(colab("a"), null)));
        when(supportService.findSupportedColabIds(isNull(), anyList())).thenReturn(Set.of());

        colabSearchService.search(null, null, null, null, List.of("pothole", "flooding"), null, null, 20, null);

        ArgumentCaptor<GeoSearchCriteria> captor = ArgumentCaptor.forClass(GeoSearchCriteria.class);
        verify(colabGeoSearchRepository).search(captor.capture(), isNull(), eq(21));
        verify(categoryService).requireActiveSlugs(List.of("pothole", "flooding"));
        assertEquals(List.of("pothole", "flooding"), captor.getValue().categorySlugs());
        assertNull(captor.getValue().nearLng());
    }

    @Test
    void paginatesByDistanceCursor() {
        stubMapping();
        ColabDistanceHit first = new ColabDistanceHit(colab("a"), 100L);
        ColabDistanceHit second = new ColabDistanceHit(colab("b"), 200L);
        ColabDistanceHit third = new ColabDistanceHit(colab("c"), 300L);
        when(colabGeoSearchRepository.search(any(), isNull(), eq(3)))
                .thenReturn(List.of(first, second, third));
        when(supportService.findSupportedColabIds(isNull(), anyList())).thenReturn(Set.of());

        ColabSearchPageResponse firstPage = colabSearchService.search(
                -20.14, -44.88, 3.0, null, null, null, null, 2, null
        );

        assertEquals(2, firstPage.items().size());
        String next = firstPage.nextPageToken();
        GeoSearchCursor cursor = GeoSearchCursor.decode(next);
        assertEquals(200, cursor.distanceMeters());
        assertEquals("b", cursor.id());

        when(colabGeoSearchRepository.search(any(), eq(cursor), eq(3)))
                .thenReturn(List.of(third));

        ColabSearchPageResponse secondPage = colabSearchService.search(
                -20.14, -44.88, 3.0, null, null, null, next, 2, null
        );
        assertEquals(1, secondPage.items().size());
        assertEquals("c", secondPage.items().get(0).colab().id());
        assertNull(secondPage.nextPageToken());
    }

    @Test
    void unknownCategoryIsBadRequest() {
        doThrow(new IllegalArgumentException("Categoria inexistente: missing"))
                .when(categoryService).requireActiveSlugs(List.of("missing"));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> colabSearchService.search(-20.14, -44.88, 3.0, null, List.of("missing"), null, null, 20, null)
        );
        assertTrue(exception.getMessage().contains("Categoria inexistente"));
    }

    @Test
    void emptySearchIsBadRequest() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> colabSearchService.search(null, null, null, null, null, null, null, 20, null)
        );
        assertTrue(exception.getMessage().contains("category") || exception.getMessage().contains("q"));
    }

    @Test
    void textSearchOmitsDistanceAndPaginatesByCreatedAt() {
        stubMapping();
        Colab older = colab("a").setCreatedAt(Instant.parse("2026-01-01T00:00:00Z"));
        Colab newer = colab("b").setCreatedAt(Instant.parse("2026-01-02T00:00:00Z"));
        when(colabGeoSearchRepository.search(any(), isNull(), eq(2)))
                .thenReturn(List.of(new ColabDistanceHit(newer, null), new ColabDistanceHit(older, null)));
        when(supportService.findSupportedColabIds(isNull(), anyList())).thenReturn(Set.of());

        ColabSearchPageResponse firstPage = colabSearchService.search(
                null, null, null, null, null, "Centro", null, 1, null
        );

        assertEquals(1, firstPage.items().size());
        assertEquals("b", firstPage.items().get(0).colab().id());
        assertNull(firstPage.items().get(0).distanceMeters());
        FilterSearchCursor cursor = FilterSearchCursor.decode(firstPage.nextPageToken());
        assertEquals("b", cursor.id());

        when(colabGeoSearchRepository.search(any(), eq(cursor), eq(2)))
                .thenReturn(List.of(new ColabDistanceHit(older, null)));

        ColabSearchPageResponse secondPage = colabSearchService.search(
                null, null, null, null, null, "Centro", firstPage.nextPageToken(), 1, null
        );
        assertEquals(1, secondPage.items().size());
        assertEquals("a", secondPage.items().get(0).colab().id());
        assertNull(secondPage.nextPageToken());
    }

    @Test
    void invalidSizeFallsBackToDefault() {
        stubMapping();
        when(colabGeoSearchRepository.search(any(), isNull(), eq(21))).thenReturn(List.of());
        when(supportService.findSupportedColabIds(isNull(), anyList())).thenReturn(Set.of());

        colabSearchService.search(-20.14, -44.88, 3.0, null, null, null, null, -5, null);

        verify(colabGeoSearchRepository).search(any(), isNull(), eq(21));
    }

    private void stubMapping() {
        when(userService.getUserById(any())).thenReturn(
                User.create("user-1", "lucas", "lucas@email.com")
        );
        when(cloudinaryService.getImageUrl(any())).thenReturn("https://img.example/a.jpg");
        when(categoryService.getCategoriesById(anyList())).thenReturn(List.of(
                new Category().setSlug("pothole").setName("Buraco").setActive(true)
        ));
    }

    private Colab colab(String id) {
        return new Colab()
                .setId(id)
                .setUserId("user-1")
                .setTitle("Buraco na rua")
                .setDescription("Grande")
                .setCategories(List.of("pothole"))
                .setSupportCount(1)
                .setStatus(EColabStatus.CREATED)
                .setCreatedAt(Instant.parse("2026-01-01T00:00:00Z"))
                .setUpdatedAt(Instant.parse("2026-01-01T00:00:00Z"))
                .setImageKey("img")
                .setLocation(new Location(
                        "Praça",
                        null,
                        "Point",
                        new Address("Rua A", "10", "Centro", "Divinópolis", "MG", "35500000"),
                        new double[]{-44.88, -20.14}
                ));
    }
}
