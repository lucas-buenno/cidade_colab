package tech.cidade.colab.exception;

public class InvalidResetLinkException extends IllegalArgumentException {

    public InvalidResetLinkException() {
        super("Link inválido ou expirado");
    }
}
