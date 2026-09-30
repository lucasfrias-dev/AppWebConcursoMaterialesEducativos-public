package ar.edu.unnoba.appweb_concurso_materiales_educativos.exception;

public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
