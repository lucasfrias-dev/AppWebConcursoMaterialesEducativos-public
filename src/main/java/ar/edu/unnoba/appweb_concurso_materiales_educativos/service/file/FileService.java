package ar.edu.unnoba.appweb_concurso_materiales_educativos.service.file;

import ar.edu.unnoba.appweb_concurso_materiales_educativos.exception.FileStorageException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.UUID;

@Service
public class FileService {

    @Value("${app.upload.dir:uploads/}")
    private String uploadDir;

    /**
     * Guarda un archivo MultipartFile de forma segura en el directorio configurado.
     *
     * @param file El archivo cargado a guardar.
     * @return El path relativo del archivo guardado.
     * @throws FileStorageException Si no se puede almacenar el archivo o el nombre es inválido.
     */
    public String saveFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        String rawFilename = StringUtils.cleanPath(Objects.requireNonNullElse(file.getOriginalFilename(), "archivo.pdf"));

        // Validar que no contenga secuencias de path traversal
        if (rawFilename.contains("..")) {
            throw new FileStorageException("El nombre del archivo contiene una ruta no válida: " + rawFilename);
        }

        // Sanitizar caracteres especiales conservando la extensión
        String cleanBase = rawFilename.replaceAll("[^a-zA-Z0-9._-]", "_");
        String uniqueFilename = UUID.randomUUID() + "_" + cleanBase;

        try {
            Path targetDir = Paths.get(uploadDir).toAbsolutePath().normalize();
            if (!Files.exists(targetDir)) {
                Files.createDirectories(targetDir);
            }

            Path targetLocation = targetDir.resolve(uniqueFilename).normalize();

            // Asegurarse de que el archivo resuelto siga dentro del directorio destino
            if (!targetLocation.startsWith(targetDir)) {
                throw new FileStorageException("No se puede almacenar el archivo fuera del directorio configurado.");
            }

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
            }

            // Retornar la ruta normalizada relativa para almacenar en BD
            Path relativePath = Paths.get(uploadDir).resolve(uniqueFilename).normalize();
            return relativePath.toString().replace("\\", "/");
        } catch (IOException e) {
            throw new FileStorageException("No se pudo almacenar el archivo " + rawFilename + ". ¡Inténtalo de nuevo!", e);
        }
    }
}

