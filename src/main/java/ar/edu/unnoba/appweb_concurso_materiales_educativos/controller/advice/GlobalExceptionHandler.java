package ar.edu.unnoba.appweb_concurso_materiales_educativos.controller.advice;

import ar.edu.unnoba.appweb_concurso_materiales_educativos.exception.FileStorageException;
import ar.edu.unnoba.appweb_concurso_materiales_educativos.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Clase para manejar excepciones globales en toda la aplicación.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(FileStorageException.class)
    public String handleFileStorageException(FileStorageException ex, RedirectAttributes redirectAttributes) {
        logger.warn("Error de almacenamiento de archivo: {}", ex.getMessage());
        redirectAttributes.addFlashAttribute("error", ex.getMessage());
        return "redirect:/concursante/postular-material";
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleMaxSizeException(MaxUploadSizeExceededException ex, RedirectAttributes redirectAttributes) {
        logger.warn("Tamaño máximo de archivo superado: {}", ex.getMessage());
        redirectAttributes.addFlashAttribute("error", "El archivo excede el tamaño máximo permitido de 16MB.");
        return "redirect:/concursante/postular-material";
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public String handleResourceNotFoundException(ResourceNotFoundException ex, RedirectAttributes redirectAttributes) {
        logger.warn("Recurso no encontrado: {}", ex.getMessage());
        redirectAttributes.addFlashAttribute("error", ex.getMessage());
        return "redirect:/";
    }
}
