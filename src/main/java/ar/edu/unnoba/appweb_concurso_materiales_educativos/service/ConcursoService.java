package ar.edu.unnoba.appweb_concurso_materiales_educativos.service;

import ar.edu.unnoba.appweb_concurso_materiales_educativos.model.Concurso;

import java.time.LocalDateTime;
import java.util.List;

public interface ConcursoService {

    //Recupera el concurso actual.
    Concurso getConcursoActual();

    //Recupera el concurso más reciente.
    Concurso getUltimoConcurso();

    //Recupera el concurso asociado a una edición.
    Concurso getConcursoByEdicion(String edicion);

    //Recupera todos los concursos anteriores al actual.
    List<Concurso> getConcursosFinalizadosMenosUltimo();

    List<Concurso> getAllConcursosFinalizados();

    Concurso getUltimoConcursoFinalizado();

    //Crea un nuevo concurso.
    Concurso createConcurso(Concurso concurso);

    String generateEdicion();

    //Cierra un concurso.
    void cerrarConcurso(String edicion);

    //Reabre un concurso.
    void reabrirConcurso(String edicion, LocalDateTime fechaFin);

    void updateFecha(String edicion, String campo, LocalDateTime fecha);
}
