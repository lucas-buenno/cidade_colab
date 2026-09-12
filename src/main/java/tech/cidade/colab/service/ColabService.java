package tech.cidade.colab.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import tech.cidade.colab.document.Colab;
import tech.cidade.colab.document.User;
import tech.cidade.colab.dto.Location;
import tech.cidade.colab.dto.request.CreateColabRequest;
import tech.cidade.colab.dto.response.CategoryResponse;
import tech.cidade.colab.dto.response.ColabResponse;
import tech.cidade.colab.dto.response.UserResponse;
import tech.cidade.colab.enums.EColabStatus;
import tech.cidade.colab.repository.ColabRepository;


import java.time.Instant;
import java.util.List;

@Slf4j
@Service
public class ColabService extends AbstractColabService{

    private final ColabRepository colabRepository;
    private final SupportService supportService;

    public ColabService(CloudinaryService cloudinaryService, UserService userService, CategoryService categoryService, ColabRepository colabRepository, SupportService supportService) {
        super(cloudinaryService, userService, categoryService);
        this.colabRepository = colabRepository;
        this.supportService = supportService;
    }

    public String createColab(CreateColabRequest request, String authenticatedUserId) {
        log.info("Criando colab com os dados: {}", request);
        Colab colabToSave = new Colab()
                .setId(request.colabId())
                .setUserId(authenticatedUserId)
                .setTitle(request.title())
                .setDescription(request.description())
                .setCategories(request.categoriesSlugs())
                .setSupportCount(0)
                .setCreatedAt(Instant.now())
                .setUpdatedAt(Instant.now())
                .setStatus(EColabStatus.CREATED)
                .setLocation(Location.from(request.location()))
                .setImageKey(request.imageKey());

        Colab created = colabRepository.save(colabToSave);
        log.info("Colab criado com sucesso: {} - Criado em: {}", created, created.getCreatedAt());
        supportService.updateSupport(created.getId(), authenticatedUserId);
        return created.getId();
    }

    public ColabResponse getById(String colabId) {
        log.info("Buscando colab com id: {}", colabId);
        Colab colab = colabRepository.findById(colabId).orElseThrow(() -> new RuntimeException("Colab não encontrado com id: " + colabId));
        log.info("Colab encontrado: {}", colab);

        ColabResponse response = generateColabResponse(colab);
        log.info("ColabResponse mapeado: {}", response);
        return response;
    }

    public UserResponse getColabsByUserId(String userId) {
        log.info("Buscando usuário com id: {}", userId);
        User user = userService.getUserById(userId);
        List<ColabResponse> userColabs = getColabs(userId);
        log.info("Usuário encontrado: {} - Quantidade de colabs: {}", user, userColabs.size());
        return new UserResponse(user.getUsername(), user.getCreatedAt(), userColabs);
    }

    private List<ColabResponse> getColabs(String userId) {
        log.info("Buscando colabs do usuário com id: {}", userId);
        List<Colab> colabs = colabRepository.findByUserIdOrderByCreatedAtDesc(userId);
        log.info("Colabs encontrados: {}", colabs.size());

        List<ColabResponse> responses = colabs.stream()
                .map(this::generateColabResponse)
                .toList();
        log.info("Colabs mapeados para ColabResponse: {}", responses.size());
        return responses;
    }

    public List<Colab> findRecentColabs(int limit) {
        return colabRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, limit));
    }

    public List<Colab> findRecentColabsBeforeId(String id, int limit) {
        return colabRepository.findByIdLessThanOrderByCreatedAtDesc(id, PageRequest.of(0, limit));
    }


    public List<Colab> findAllColabs() {
        return (List<Colab>) colabRepository.findAll();
    }

}
