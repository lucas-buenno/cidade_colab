package tech.cidade.colab.exception;

public class InvalidEmailException extends IllegalArgumentException {

    public InvalidEmailException() {
        super("E-mail inválido");
    }
}
