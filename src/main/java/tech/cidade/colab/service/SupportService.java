package tech.cidade.colab.service;

import com.mongodb.client.result.DeleteResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import tech.cidade.colab.document.Colab;
import tech.cidade.colab.document.ColabSupport;
import tech.cidade.colab.repository.ColabRepository;
import tech.cidade.colab.repository.ColabSupportRepository;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class SupportService {

    private final ColabRepository colabRepository;
    private final MongoTemplate mongoTemplate;
    private final ColabSupportRepository colabSupportRepository;


    public void updateSupport(String colabId, String userId) {
        log.info("Atualizando suporte do colab {} pelo usuário {}", colabId, userId);
        colabRepository.findById(colabId).orElseThrow(() -> new RuntimeException("Colab não encontrado: " + colabId));
        boolean alreadySupported = colabSupportRepository.existsByColabIdAndUserId(colabId, userId);
        if (alreadySupported) {
            log.info("Usuário {} já apoiou o colab {}, removendo suporte", userId, colabId);
            removeSupport(colabId, userId);
            return;
        }
        log.info("Usuário {} ainda não apoiou o colab {}, adicionando suporte", userId, colabId);
        addSupport(colabId, userId);
    }

    private void addSupport(String colabId, String userId) {
        try {
            ColabSupport support = new ColabSupport();
            support.setColabId(colabId);
            support.setUserId(userId);
            support.setCreatedAt(Instant.now());
            colabSupportRepository.save(support);
        } catch (Exception e) {
            log.warn("Usuário {} já apoiou o colab {}", userId, colabId);
            return;
        }

        mongoTemplate.updateFirst(
                Query.query(Criteria.where("_id").is(colabId)),
                new Update().inc("supportCount", 1).set("updatedAt", Instant.now()),
                Colab.class
        );
        log.info("Suporte adicionado ao colab {} pelo usuário {}", colabId, userId);
    }

    private void removeSupport(String colabId, String userId) {
        DeleteResult result = mongoTemplate.remove(
                Query.query(Criteria.where("colabId").is(colabId).and("userId").is(userId)),
                ColabSupport.class
        );

        if (result.getDeletedCount() == 0) {
            log.warn("Nenhum suporte encontrado para remover: colab {} usuário {}", colabId, userId);
            return;
        }

        mongoTemplate.updateFirst(
                Query.query(Criteria.where("_id").is(colabId)),
                new Update().inc("supportCount", -1).set("updatedAt", Instant.now()),
                Colab.class
        );
        log.info("Suporte removido do colab {} pelo usuário {}", colabId, userId);
    }
}
