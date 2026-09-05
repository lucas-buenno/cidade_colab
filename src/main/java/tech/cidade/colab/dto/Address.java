package tech.cidade.colab.dto;

public record Address(
        String street,
        String number,
        String neighborhood,
        String city,
        String state,
        String postalCode) {

    public static Address from(String street, String number, String neighborhood, String postalCode) {
        return new Address(
                street,
                number,
                neighborhood,
                "Divinópolis", "MG",
                postalCode
        );
    }
}
