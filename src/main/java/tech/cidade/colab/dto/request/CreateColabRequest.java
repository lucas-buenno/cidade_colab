package tech.cidade.colab.dto.request;

import java.util.List;

public record CreateColabRequest(String colabId,
                                 String title,
                                 String description,
                                 List<String> categoriesSlugs,
                                 LocationRequest location,
                                 String imageKey) {
}
