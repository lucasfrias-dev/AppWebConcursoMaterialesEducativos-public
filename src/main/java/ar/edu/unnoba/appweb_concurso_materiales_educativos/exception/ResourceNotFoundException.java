package ar.edu.unnoba.appweb_concurso_materiales_educativos.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
