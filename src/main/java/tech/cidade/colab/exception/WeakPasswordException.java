package tech.cidade.colab.exception;

public class WeakPasswordException extends IllegalArgumentException {

    public WeakPasswordException() {
        super("A senha deve conter ao menos 8 caracteres");
    }
}
