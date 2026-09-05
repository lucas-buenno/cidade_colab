package tech.cidade.colab.dto.request;

public record LocationRequest(String name,
                              String reference,
                              String type,
                              String street,
                              String number,
                              String neighborhood,
                              String postalCode,
                              double[] coordinates) {
}
