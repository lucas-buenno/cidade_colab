package tech.cidade.colab.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tech.cidade.colab.document.Colab;
import tech.cidade.colab.dto.response.ColabResponse;
import tech.cidade.colab.dto.response.FeedPageResponse;
import tech.cidade.colab.utils.CursorUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static tech.cidade.colab.utils.CursorUtils.validateAndNormalizeSize;

@Service
@Slf4j
public class FeedService extends AbstractColabService{

    private final ColabService colabService;
    private final SupportService supportService;

    public FeedService(CloudinaryService cloudinaryService, UserService userService, CategoryService categoryService, ColabService colabService, SupportService supportService) {
        super(cloudinaryService, userService, categoryService);
        this.colabService = colabService;
        this.supportService = supportService;
    }

    public FeedPageResponse getFeed(String pageToken, int size, String authenticatedUserId) {
        int safeSize = validateAndNormalizeSize(size);
        String cursorId = CursorUtils.decode(pageToken);

        log.info("Buscando feed de colabs com pageToken: {} e size: {}", pageToken, safeSize);

        int querySize = safeSize + 1;

        List<Colab> colabs = (cursorId == null)
                ? colabService.findRecentColabs(querySize)
                : colabService.findRecentColabsBeforeId(cursorId, querySize);

        boolean hasNextPage = colabs.size() > safeSize;
        List<Colab> pageItems = hasNextPage
                ? new ArrayList<>(colabs.subList(0, safeSize))
                : colabs;

        List<ColabResponse> colabResponses = mapToColabResponse(pageItems, authenticatedUserId);

        String nextPageToken = hasNextPage
                ? CursorUtils.encode(pageItems.get(pageItems.size() - 1).getId())
                : null;

        log.info("Feed de colabs mapeado: Itens: {} - hasNextPage: {} - nextPageToken: {}",
                colabResponses.size(), hasNextPage, nextPageToken);

        return new FeedPageResponse(colabResponses, nextPageToken);
    }

    private List<ColabResponse> mapToColabResponse(List<Colab> pageItems, String authenticatedUserId) {
        Set<String> supportedIds = supportService.findSupportedColabIds(
                authenticatedUserId,
                pageItems.stream().map(Colab::getId).toList()
        );
        return pageItems.stream()
                .map(colab -> generateColabResponse(colab, supportedIds.contains(colab.getId())))
                .toList();
    }
}