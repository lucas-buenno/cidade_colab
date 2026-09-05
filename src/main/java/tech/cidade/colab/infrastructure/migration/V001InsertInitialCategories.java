package tech.cidade.colab.infrastructure.migration;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import org.springframework.data.mongodb.core.MongoTemplate;
import tech.cidade.colab.document.Category;
import tech.cidade.colab.enums.ECategoryGroup;

import java.time.Instant;
import java.util.List;

@ChangeUnit(
        id = "v001-insert-initial-categories",
        order = "001",
        author = "admin"
)
public class V001InsertInitialCategories {

    private final MongoTemplate mongoTemplate;

    public V001InsertInitialCategories(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Execution
    public void execute() {
        Instant now = Instant.now();

        mongoTemplate.insertAll(List.of(

                category(
                        "pothole",
                        "Buraco na via",
                        "Buraco, depressão ou dano pontual no asfalto ou pavimento da via.",
                        ECategoryGroup.ROADS_AND_SIDEWALKS,
                        List.of(
                                "buraco",
                                "buraco na rua",
                                "buraco no asfalto",
                                "cratera",
                                "pavimento danificado"
                        ),
                        now
                ),

                category(
                        "road-damage",
                        "Dano na via",
                        "Danos gerais em ruas, avenidas, estradas ou pavimentação.",
                        ECategoryGroup.ROADS_AND_SIDEWALKS,
                        List.of(
                                "rua danificada",
                                "asfalto ruim",
                                "via danificada",
                                "pavimento quebrado",
                                "estrada danificada"
                        ),
                        now
                ),

                category(
                        "sidewalk-damage",
                        "Calçada danificada",
                        "Buracos, desníveis, rachaduras ou danos que prejudiquem o uso da calçada.",
                        ECategoryGroup.ROADS_AND_SIDEWALKS,
                        List.of(
                                "calçada quebrada",
                                "calçada danificada",
                                "calçada esburacada",
                                "passeio danificado",
                                "desnível na calçada"
                        ),
                        now
                ),

                category(
                        "street-sign-damage",
                        "Placa de sinalização danificada",
                        "Placa de trânsito, identificação de rua ou sinalização urbana ausente, caída ou ilegível.",
                        ECategoryGroup.ROADS_AND_SIDEWALKS,
                        List.of(
                                "placa quebrada",
                                "placa caída",
                                "placa ilegível",
                                "falta de sinalização",
                                "sinalização danificada"
                        ),
                        now
                ),

                category(
                        "traffic-light-malfunction",
                        "Semáforo com defeito",
                        "Semáforo apagado, piscando incorretamente ou funcionando de maneira irregular.",
                        ECategoryGroup.ROADS_AND_SIDEWALKS,
                        List.of(
                                "semáforo apagado",
                                "semáforo quebrado",
                                "semáforo piscando",
                                "sinal de trânsito com defeito"
                        ),
                        now
                ),

                category(
                        "streetlight-outage",
                        "Iluminação pública apagada",
                        "Poste, luminária ou trecho de via pública sem iluminação.",
                        ECategoryGroup.PUBLIC_LIGHTING,
                        List.of(
                                "poste apagado",
                                "lâmpada queimada",
                                "luz apagada",
                                "iluminação apagada",
                                "iluminação pública"
                        ),
                        now
                ),

                category(
                        "streetlight-flickering",
                        "Iluminação pública com falha",
                        "Lâmpada ou luminária pública piscando, fraca ou funcionando de modo intermitente.",
                        ECategoryGroup.PUBLIC_LIGHTING,
                        List.of(
                                "luz piscando",
                                "poste piscando",
                                "lâmpada piscando",
                                "luz fraca",
                                "iluminação com defeito"
                        ),
                        now
                ),

                category(
                        "damaged-utility-pole",
                        "Poste danificado",
                        "Poste inclinado, quebrado, com risco aparente ou danificado em área pública.",
                        ECategoryGroup.PUBLIC_LIGHTING,
                        List.of(
                                "poste quebrado",
                                "poste torto",
                                "poste inclinado",
                                "poste danificado",
                                "poste com risco de queda"
                        ),
                        now
                ),

                category(
                        "illegal-dumping",
                        "Descarte irregular de lixo",
                        "Descarte de lixo, entulho, móveis ou resíduos em local inadequado.",
                        ECategoryGroup.SANITATION,
                        List.of(
                                "lixo jogado",
                                "descarte irregular",
                                "entulho",
                                "lixo na rua",
                                "móveis descartados"
                        ),
                        now
                ),

                category(
                        "garbage-accumulation",
                        "Acúmulo de lixo",
                        "Grande quantidade de resíduos acumulados em via, calçada, praça ou área pública.",
                        ECategoryGroup.SANITATION,
                        List.of(
                                "lixo acumulado",
                                "muito lixo",
                                "sujeira",
                                "resíduos acumulados",
                                "lixo na calçada"
                        ),
                        now
                ),

                category(
                        "sewage-leak",
                        "Vazamento de esgoto",
                        "Esgoto vazando, retornando ou escoando em via pública.",
                        ECategoryGroup.SANITATION,
                        List.of(
                                "esgoto vazando",
                                "esgoto na rua",
                                "mau cheiro",
                                "vazamento de esgoto",
                                "esgoto estourado"
                        ),
                        now
                ),

                category(
                        "clogged-drain",
                        "Bueiro entupido",
                        "Bueiro, boca de lobo ou sistema de drenagem obstruído.",
                        ECategoryGroup.SANITATION,
                        List.of(
                                "bueiro entupido",
                                "boca de lobo entupida",
                                "dreno entupido",
                                "ralo entupido",
                                "drenagem obstruída"
                        ),
                        now
                ),

                category(
                        "flooding",
                        "Alagamento",
                        "Acúmulo de água em via pública, calçada ou espaço urbano que dificulte a mobilidade.",
                        ECategoryGroup.SANITATION,
                        List.of(
                                "alagamento",
                                "rua alagada",
                                "enchente",
                                "água acumulada",
                                "inundação"
                        ),
                        now
                ),

                category(
                        "water-leak",
                        "Vazamento de água",
                        "Vazamento de água limpa em rua, calçada ou equipamento público.",
                        ECategoryGroup.WATER_INFRASTRUCTURE,
                        List.of(
                                "água vazando",
                                "vazamento na rua",
                                "cano estourado",
                                "vazamento de água",
                                "água na calçada"
                        ),
                        now
                ),

                category(
                        "damaged-manhole-cover",
                        "Tampa de bueiro danificada",
                        "Tampa de bueiro, poço de visita ou caixa de inspeção quebrada, solta ou ausente.",
                        ECategoryGroup.WATER_INFRASTRUCTURE,
                        List.of(
                                "tampa de bueiro quebrada",
                                "tampa de bueiro solta",
                                "bueiro sem tampa",
                                "tampa ausente",
                                "tampa danificada"
                        ),
                        now
                ),

                category(
                        "overgrown-lot",
                        "Lote com mato alto",
                        "Terreno ou lote urbano com vegetação excessiva e necessidade de limpeza.",
                        ECategoryGroup.URBAN_VEGETATION,
                        List.of(
                                "lote com mato alto",
                                "terreno sujo",
                                "terreno abandonado",
                                "mato alto",
                                "lote sem limpeza"
                        ),
                        now
                ),

                category(
                        "overgrown-vegetation",
                        "Vegetação excessiva em área pública",
                        "Mato alto, capim ou vegetação que prejudica o uso de calçadas, ruas, praças e demais áreas públicas.",
                        ECategoryGroup.URBAN_VEGETATION,
                        List.of(
                                "mato na calçada",
                                "vegetação alta",
                                "capim alto",
                                "mato na praça",
                                "poda necessária"
                        ),
                        now
                ),

                category(
                        "fallen-tree",
                        "Árvore caída",
                        "Árvore, galho de grande porte ou vegetação caída obstruindo ou oferecendo risco em área pública.",
                        ECategoryGroup.URBAN_VEGETATION,
                        List.of(
                                "árvore caída",
                                "galho caído",
                                "árvore na rua",
                                "galho na calçada",
                                "queda de árvore"
                        ),
                        now
                ),

                category(
                        "risky-tree",
                        "Árvore com risco de queda",
                        "Árvore inclinada, comprometida ou com galhos que aparentem risco de queda.",
                        ECategoryGroup.URBAN_VEGETATION,
                        List.of(
                                "árvore inclinada",
                                "árvore com risco",
                                "galho perigoso",
                                "árvore podre",
                                "risco de queda"
                        ),
                        now
                ),

                category(
                        "park-maintenance-issue",
                        "Problema de manutenção em praça",
                        "Estruturas, bancos, iluminação, equipamentos ou limpeza inadequada em praças e parques.",
                        ECategoryGroup.PUBLIC_SPACES,
                        List.of(
                                "praça abandonada",
                                "praça suja",
                                "banco quebrado",
                                "parque danificado",
                                "manutenção da praça"
                        ),
                        now
                ),

                category(
                        "damaged-playground",
                        "Parquinho danificado",
                        "Brinquedos, equipamentos ou estruturas infantis danificadas em área pública.",
                        ECategoryGroup.PUBLIC_SPACES,
                        List.of(
                                "parquinho quebrado",
                                "brinquedo quebrado",
                                "playground danificado",
                                "balanço quebrado",
                                "escorregador quebrado"
                        ),
                        now
                ),

                category(
                        "blocked-sidewalk",
                        "Calçada obstruída",
                        "Calçada bloqueada por objetos, resíduos, obras, veículos ou obstáculos que impeçam a circulação.",
                        ECategoryGroup.ACCESSIBILITY_AND_MOBILITY,
                        List.of(
                                "calçada bloqueada",
                                "calçada obstruída",
                                "passeio bloqueado",
                                "obstáculo na calçada",
                                "calçada sem passagem"
                        ),
                        now
                ),

                category(
                        "accessibility-issue",
                        "Problema de acessibilidade",
                        "Ausência ou dano em rampa, piso tátil, travessia, rebaixamento de guia ou outra estrutura de acessibilidade.",
                        ECategoryGroup.ACCESSIBILITY_AND_MOBILITY,
                        List.of(
                                "rampa quebrada",
                                "sem rampa",
                                "piso tátil",
                                "acessibilidade",
                                "guia rebaixada"
                        ),
                        now
                ),

                category(
                        "damaged-bus-stop",
                        "Ponto de ônibus danificado",
                        "Abrigo, banco, cobertura, placa ou estrutura de ponto de ônibus danificada.",
                        ECategoryGroup.ACCESSIBILITY_AND_MOBILITY,
                        List.of(
                                "ponto de ônibus quebrado",
                                "abrigo de ônibus danificado",
                                "ponto sem cobertura",
                                "banco do ponto quebrado",
                                "placa do ponto danificada"
                        ),
                        now
                ),

                category(
                        "abandoned-vehicle",
                        "Veículo abandonado",
                        "Veículo aparentemente abandonado em via pública por período prolongado.",
                        ECategoryGroup.PUBLIC_SAFETY,
                        List.of(
                                "carro abandonado",
                                "veículo abandonado",
                                "moto abandonada",
                                "carro parado há muito tempo",
                                "sucata na rua"
                        ),
                        now
                ),

                category(
                        "damaged-public-property",
                        "Patrimônio público danificado",
                        "Dano em bancos, lixeiras, placas, equipamentos, monumentos ou outras estruturas públicas.",
                        ECategoryGroup.PUBLIC_SAFETY,
                        List.of(
                                "patrimônio danificado",
                                "lixeira quebrada",
                                "equipamento público quebrado",
                                "vandalismo",
                                "bem público danificado"
                        ),
                        now
                ),

                category(
                        "dead-animal-removal",
                        "Animal morto em via pública",
                        "Animal morto em rua, calçada, praça ou outra área pública que necessite de remoção.",
                        ECategoryGroup.ANIMAL_WELFARE,
                        List.of(
                                "animal morto",
                                "cachorro morto",
                                "gato morto",
                                "animal na rua",
                                "remover animal morto"
                        ),
                        now
                ),

                category(
                        "animal-welfare-issue",
                        "Situação de bem-estar animal",
                        "Animal em situação de risco, abandono, maus-tratos aparentes ou necessidade de atendimento público.",
                        ECategoryGroup.ANIMAL_WELFARE,
                        List.of(
                                "animal abandonado",
                                "animal ferido",
                                "maus-tratos",
                                "cachorro solto",
                                "animal em risco"
                        ),
                        now
                ),

                category(
                        "other",
                        "Outro problema urbano",
                        "Ocorrência urbana que não se encaixa nas categorias disponíveis.",
                        ECategoryGroup.OTHER,
                        List.of(
                                "outro",
                                "outros problemas",
                                "problema urbano"
                        ),
                        now
                )
        ));
    }

    @RollbackExecution
    public void rollback() {
        mongoTemplate.remove(
                new org.springframework.data.mongodb.core.query.Query(
                        org.springframework.data.mongodb.core.query.Criteria.where("_id")
                                .in(List.of(
                                        "pothole",
                                        "road-damage",
                                        "sidewalk-damage",
                                        "street-sign-damage",
                                        "traffic-light-malfunction",
                                        "streetlight-outage",
                                        "streetlight-flickering",
                                        "damaged-utility-pole",
                                        "illegal-dumping",
                                        "garbage-accumulation",
                                        "sewage-leak",
                                        "clogged-drain",
                                        "flooding",
                                        "water-leak",
                                        "damaged-manhole-cover",
                                        "overgrown-lot",
                                        "overgrown-vegetation",
                                        "fallen-tree",
                                        "risky-tree",
                                        "park-maintenance-issue",
                                        "damaged-playground",
                                        "blocked-sidewalk",
                                        "accessibility-issue",
                                        "damaged-bus-stop",
                                        "abandoned-vehicle",
                                        "damaged-public-property",
                                        "dead-animal-removal",
                                        "animal-welfare-issue",
                                        "other"
                                ))
                ),
                Category.class
        );
    }

    private Category category(
            String slug,
            String name,
            String description,
            ECategoryGroup group,
            List<String> keywords,
            Instant now
    ) {
        return new Category()
                .setSlug(slug)
                .setName(name)
                .setDescription(description)
                .setGroup(group)
                .setKeywords(keywords)
                .setActive(true)
                .setCreatedAt(now)
                .setUpdatedAt(now);
    }
}