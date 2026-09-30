package ar.edu.unnoba.appweb_concurso_materiales_educativos.service;

import ar.edu.unnoba.appweb_concurso_materiales_educativos.model.Concurso;
import ar.edu.unnoba.appweb_concurso_materiales_educativos.repository.ConcursoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConcursoServiceTest {

    @Mock
    private ConcursoRepository concursoRepository;

    @InjectMocks
    private ConcursoServiceImpl concursoService;

    private Concurso concurso;

    @BeforeEach
    void setUp() {
        concurso = new Concurso();
        concurso.setId(1L);
        concurso.setEdicion("I");
        concurso.setFechaInicio(LocalDateTime.now().minusDays(1));
        concurso.setFechaFinPostulaciones(LocalDateTime.now().plusDays(5));
        concurso.setFechaInicioVotacion(LocalDateTime.now().plusDays(6));
        concurso.setFechaFinVotacion(LocalDateTime.now().plusDays(10));
        concurso.setFechaFin(LocalDateTime.now().plusDays(12));
    }

    @Test
    void testGetConcursoActual() {
        when(concursoRepository.findCurrentConcurso(any(LocalDateTime.class))).thenReturn(concurso);

        Concurso actual = concursoService.getConcursoActual();

        assertNotNull(actual);
        assertEquals("I", actual.getEdicion());
        verify(concursoRepository, times(1)).findCurrentConcurso(any(LocalDateTime.class));
    }

    @Test
    void testGetUltimoConcurso() {
        when(concursoRepository.findLastConcurso()).thenReturn(concurso);

        Concurso ultimo = concursoService.getUltimoConcurso();

        assertNotNull(ultimo);
        assertEquals(1L, ultimo.getId());
        verify(concursoRepository, times(1)).findLastConcurso();
    }

    @Test
    void testGetConcursoByEdicion() {
        when(concursoRepository.findConcursoByEdicion("I")).thenReturn(concurso);

        Concurso found = concursoService.getConcursoByEdicion("I");

        assertNotNull(found);
        assertEquals("I", found.getEdicion());
    }

    @Test
    void testGenerateEdicion() {
        when(concursoRepository.findLastConcurso()).thenReturn(concurso);

        concurso.setEdicion("III Edicion");
        String nextEdicion = concursoService.generateEdicion();

        assertEquals("IV Edicion", nextEdicion);
    }

    @Test
    void testCreateConcurso_FirstEdition() {
        when(concursoRepository.findLastConcurso()).thenReturn(null);
        when(concursoRepository.save(any(Concurso.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Concurso nuevo = new Concurso();
        nuevo.setFechaInicio(LocalDateTime.now());
        nuevo.setFechaFinPostulaciones(LocalDateTime.now().plusDays(5));
        nuevo.setFechaInicioVotacion(LocalDateTime.now().plusDays(6));
        nuevo.setFechaFinVotacion(LocalDateTime.now().plusDays(10));
        nuevo.setFechaFin(LocalDateTime.now().plusDays(12));

        Concurso created = concursoService.createConcurso(nuevo);

        assertNotNull(created);
        assertEquals("III Edicion", created.getEdicion());
        verify(concursoRepository, times(1)).save(nuevo);
    }

    @Test
    void testCreateConcurso_NextEdition() {
        concurso.setEdicion("III Edicion");
        when(concursoRepository.findLastConcurso()).thenReturn(concurso);
        when(concursoRepository.save(any(Concurso.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Concurso nuevo = new Concurso();
        nuevo.setFechaInicio(LocalDateTime.now().plusDays(15));
        nuevo.setFechaFinPostulaciones(LocalDateTime.now().plusDays(20));
        nuevo.setFechaInicioVotacion(LocalDateTime.now().plusDays(21));
        nuevo.setFechaFinVotacion(LocalDateTime.now().plusDays(25));
        nuevo.setFechaFin(LocalDateTime.now().plusDays(27));

        Concurso created = concursoService.createConcurso(nuevo);

        assertNotNull(created);
        assertEquals("IV Edicion", created.getEdicion());
    }
}
