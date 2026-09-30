package ar.edu.unnoba.appweb_concurso_materiales_educativos.service.pdf;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class PdfService {

    @Value("${app.upload.dir:uploads/}")
    private String uploadDir;

    /**
     * Convierte la primera página de un archivo PDF en una imagen PNG y la cachea en disco.
     *
     * @param pdfFilePath La ruta del archivo PDF que se va a convertir.
     * @return El archivo de imagen PNG generado o cacheado.
     * @throws IOException Si ocurre un error durante la carga o escritura del archivo.
     */
    public File convertPdfToImage(String pdfFilePath) throws IOException {
        if (pdfFilePath == null || pdfFilePath.isBlank()) {
            throw new FileNotFoundException("Ruta de archivo PDF no especificada.");
        }

        File pdfFile = new File(pdfFilePath);
        if (!pdfFile.exists() || !pdfFile.canRead()) {
            throw new FileNotFoundException("El archivo PDF no existe o no se puede leer: " + pdfFilePath);
        }

        // Directorio de previsualizaciones
        Path previewsDir = Paths.get(uploadDir, "previews").toAbsolutePath().normalize();
        if (!Files.exists(previewsDir)) {
            Files.createDirectories(previewsDir);
        }

        File outputFile = previewsDir.resolve(pdfFile.getName() + ".png").toFile();

        // Si ya existe la previsualización generada para este archivo, se retorna de caché
        if (outputFile.exists() && outputFile.length() > 0) {
            return outputFile;
        }

        // Carga y renderizado con cierre seguro de recursos
        try (PDDocument document = PDDocument.load(pdfFile)) {
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            // 150 DPI produce excelente resolución para tarjetas/miniaturas con consumo óptimo de memoria
            BufferedImage bim = pdfRenderer.renderImageWithDPI(0, 150);
            ImageIO.write(bim, "png", outputFile);
        }

        return outputFile;
    }
}

