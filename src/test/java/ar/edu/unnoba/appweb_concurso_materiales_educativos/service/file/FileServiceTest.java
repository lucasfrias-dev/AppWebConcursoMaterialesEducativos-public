package ar.edu.unnoba.appweb_concurso_materiales_educativos.service.file;

import ar.edu.unnoba.appweb_concurso_materiales_educativos.exception.FileStorageException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FileServiceTest {

    private FileService fileService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        fileService = new FileService();
        ReflectionTestUtils.setField(fileService, "uploadDir", tempDir.toString());
    }

    @Test
    void testSaveFile_ValidFile() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-document.pdf",
                "application/pdf",
                "Dummy content".getBytes()
        );

        String savedPath = fileService.saveFile(file);
        assertNotNull(savedPath);
        assertTrue(savedPath.contains("test-document.pdf"));
    }

    @Test
    void testSaveFile_PathTraversalDetected() {
        MockMultipartFile maliciousFile = new MockMultipartFile(
                "file",
                "../../malicious.pdf",
                "application/pdf",
                "evil content".getBytes()
        );

        assertThrows(FileStorageException.class, () -> fileService.saveFile(maliciousFile));
    }

    @Test
    void testSaveFile_EmptyOrNull() {
        assertNull(fileService.saveFile(null));

        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.pdf",
                "application/pdf",
                new byte[0]
        );
        assertNull(fileService.saveFile(emptyFile));
    }
}
