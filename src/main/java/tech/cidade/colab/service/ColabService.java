package tech.cidade.colab.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tech.cidade.colab.document.Colab;
import tech.cidade.colab.dto.Location;
import tech.cidade.colab.dto.request.CreateColabRequest;
import tech.cidade.colab.enums.EColabStatus;
import tech.cidade.colab.repository.ColabRepository;


import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ColabService {

    private final ColabRepository colabRepository;
    private final CategoryService categoryService;
    private final SupportService supportService;

    public String createColab(CreateColabRequest request) {
        log.info("Criando colab com os dados: {}", request);
        Colab colabToSave = new Colab()
                .setUserId(request.userId())
                .setTitle(request.title())
                .setDescription(request.description())
                .setCategories(request.categoriesSlugs())
                .setSupportCount(0)
                .setCreatedAt(Instant.now())
                .setUpdatedAt(Instant.now())
                .setStatus(EColabStatus.CREATED)
                .setLocation(Location.from(request.location()));

        Colab created = colabRepository.save(colabToSave);
        log.info("Colab criado com sucesso: {} - Criado em: {}", created, created.getCreatedAt());
        supportService.updateSupport(created.getId(), request.userId());
        return created.getId();
    }

    public List<Colab> findAllColabs() {
        return (List<Colab>) colabRepository.findAll();
    }

}
