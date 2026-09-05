package tech.cidade.colab.dto.request;

import tech.cidade.colab.dto.Location;

import java.util.List;

public record CreateColabRequest(String userId,
                                 String title,
                                 String description,
                                 List<String> categoriesSlugs,
                                 LocationRequest location) {
}
