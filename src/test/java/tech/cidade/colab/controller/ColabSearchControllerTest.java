package tech.cidade.colab.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tech.cidade.colab.dto.response.ColabResponse;
import tech.cidade.colab.dto.response.ColabSearchItemResponse;
import tech.cidade.colab.dto.response.ColabSearchPageResponse;
import tech.cidade.colab.enums.EColabStatus;
import tech.cidade.colab.service.ColabSearchService;

import java.time.Instant;
import java.util.List;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ColabSearchControllerTest {

    @Mock
    private ColabSearchService colabSearchService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ColabSearchController(colabSearchService))
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();
    }

    @Test
    void returns200WithItems() throws Exception {
        ColabResponse colab = new ColabResponse(
                "a",
                "user-1",
                "lucas",
                "Buraco",
                "Desc",
                List.of(),
                EColabStatus.CREATED,
                1,
                false,
                null,
                Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-01-01T00:00:00Z"),
                "https://img.example/a.jpg"
        );
        when(colabSearchService.search(eq(-20.14), eq(-44.88), eq(3.0), isNull(), isNull(), isNull(), isNull(), eq(20), isNull()))
                .thenReturn(new ColabSearchPageResponse(List.of(new ColabSearchItemResponse(colab, 312L)), null));

        mockMvc.perform(get("/v1/colabs/search")
                        .param("lat", "-20.14")
                        .param("lng", "-44.88")
                        .param("radiusKm", "3")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").value("a"))
                .andExpect(jsonPath("$.items[0].distanceMeters").value(312))
                .andExpect(jsonPath("$.items[0].imageUrl").value("https://img.example/a.jpg"))
                .andExpect(jsonPath("$.nextPageToken").value(nullValue()));
    }

    @Test
    void returns200EmptyList() throws Exception {
        when(colabSearchService.search(any(), any(), any(), any(), any(), any(), any(), anyInt(), any()))
                .thenReturn(new ColabSearchPageResponse(List.of(), null));

        mockMvc.perform(get("/v1/colabs/search")
                        .param("lat", "-20.14")
                        .param("lng", "-44.88"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isEmpty());
    }

    @Test
    void returns200ForTextSearchWithoutGeo() throws Exception {
        ColabResponse colab = new ColabResponse(
                "a",
                "user-1",
                "lucas",
                "Buraco",
                "Desc",
                List.of(),
                EColabStatus.CREATED,
                1,
                false,
                null,
                Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-01-01T00:00:00Z"),
                "https://img.example/a.jpg"
        );
        when(colabSearchService.search(isNull(), isNull(), isNull(), isNull(), isNull(), eq("Centro"), isNull(), eq(20), isNull()))
                .thenReturn(new ColabSearchPageResponse(List.of(new ColabSearchItemResponse(colab, null)), null));

        mockMvc.perform(get("/v1/colabs/search")
                        .param("q", "Centro")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").value("a"))
                .andExpect(jsonPath("$.items[0].distanceMeters").value(nullValue()));
    }

    @Test
    void returns400WithClearMessage() throws Exception {
        when(colabSearchService.search(any(), any(), any(), any(), any(), any(), any(), anyInt(), any()))
                .thenThrow(new IllegalArgumentException("lat e lng devem ser enviados juntos"));

        mockMvc.perform(get("/v1/colabs/search").param("lat", "-20.14"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("lat e lng devem ser enviados juntos"));
    }
}
