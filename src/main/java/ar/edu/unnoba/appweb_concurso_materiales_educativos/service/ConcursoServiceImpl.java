package ar.edu.unnoba.appweb_concurso_materiales_educativos.service;

import ar.edu.unnoba.appweb_concurso_materiales_educativos.model.Concurso;
import ar.edu.unnoba.appweb_concurso_materiales_educativos.repository.ConcursoRepository;
import ar.edu.unnoba.appweb_concurso_materiales_educativos.utils.RomanNumerals;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class ConcursoServiceImpl implements ConcursoService{

    private final ConcursoRepository concursoRepository;

    @Autowired
    public ConcursoServiceImpl(ConcursoRepository concursoRepository) {
        this.concursoRepository = concursoRepository;
    }

    /**
     * Retorna el concurso actual basado en la fecha y hora actual.
     *
     * @return El concurso que está actualmente en curso.
     */
    @Override
    @Transactional(readOnly = true)
    public Concurso getConcursoActual() {
        return concursoRepository.findCurrentConcurso(LocalDateTime.now());
    }

    /**
     * Obtiene el último concurso registrado en la base de datos.
     *
     * @return El último concurso registrado, o null si no hay concurso registrado.
     */
    @Override
    @Transactional(readOnly = true)
    public Concurso getUltimoConcurso() {
        // Utiliza el repositorio para buscar el último concurso registrado.
        return concursoRepository.findLastConcurso();
    }


    /**
     * Retorna el concurso asociado a la edición especificada.
     *
     * @param edicion La edición del concurso que se desea obtener.
     * @return El concurso asociado a la edición especificada, si existe.
     */
    @Override
    @Transactional(readOnly = true)
    public Concurso getConcursoByEdicion(String edicion) {
        return concursoRepository.findConcursoByEdicion(edicion);
    }


    /**
     * Obtiene la lista de concursos finalizados, excluyendo el último concurso finalizado registrado en la base de datos.
     *
     * @return Una lista de concursos finalizados, excluyendo el último concurso finalizado, o una lista vacía si no hay concursos finalizados.
     */
    @Override
    @Transactional(readOnly = true)
    public List<Concurso> getConcursosFinalizadosMenosUltimo() {
        // Utiliza el repositorio para buscar los concursos finalizados, excluyendo el último concurso finalizado.
        // Se pasa la fecha y hora actual como referencia para obtener concursos finalizados hasta este momento.
        return concursoRepository.findConcursosFinalizadosMenosUltimo(LocalDateTime.now());
    }


    /**
     * Obtiene una lista de todos los concursos finalizados hasta la fecha y hora actual.
     *
     * @return Una lista de todos los concursos finalizados hasta la fecha y hora actual, o una lista vacía si no hay concursos finalizados.
     */
    @Override
    @Transactional(readOnly = true)
    public List<Concurso> getAllConcursosFinalizados() {
        // Utiliza el repositorio para buscar todos los concursos finalizados hasta la fecha y hora actual.
        return concursoRepository.findAllConcursosFinalizados(LocalDateTime.now());
    }


    /**
     * Obtiene el último concurso finalizado registrado en la base de datos.
     * Si no hay concursos finalizados, devuelve null.
     *
     * @return El último concurso finalizado, o null si no hay concursos finalizados.
     */
    @Override
    @Transactional(readOnly = true)
    public Concurso getUltimoConcursoFinalizado() {
        // Obtiene una lista de todos los concursos finalizados hasta la fecha y hora actual.
        List<Concurso> concursosFinalizados = concursoRepository.findAllConcursosFinalizados(LocalDateTime.now());

        // Verifica si la lista de concursos finalizados no está vacía.
        if (!concursosFinalizados.isEmpty()) {
            // Si hay concursos finalizados, devuelve el último de la lista.
            return concursosFinalizados.getLast();
        }

        // Si no hay concursos finalizados, devuelve null.
        return null;
    }


    /**
     * Crea un nuevo concurso en la base de datos.
     * Genera automáticamente la edición del concurso antes de guardarlo.
     *
     * @param concurso El objeto Concurso que se va a crear.
     * @return El concurso creado y guardado en la base de datos.
     */
    @Override
    @Transactional
    public Concurso createConcurso(Concurso concurso) {
        // Genera la edición del concurso automáticamente.
        concurso.setEdicion(generateEdicion());
        // Guarda el concurso en la base de datos y devuelve el objeto guardado.
        return concursoRepository.save(concurso);
    }


    /**
     * Cierra un concurso especificado por su edición.
     * Establece la fecha de cierre del concurso como la fecha y hora actuales.
     * Si el concurso ya está cerrado o no se encuentra, se lanzan excepciones correspondientes.
     *
     * @param edicion La edición del concurso que se va a cerrar.
     * @throws IllegalArgumentException Si no se encuentra ningún concurso con la edición especificada.
     * @throws IllegalStateException    Si el concurso ya está cerrado.
     */
    @Override
    @Transactional
    public void cerrarConcurso(String edicion) {
        // Busca el concurso con la edición especificada en la base de datos
        Concurso concurso = concursoRepository.findConcursoByEdicion(edicion);

        // Verifica si se encontró el concurso
        if (concurso == null) {
            throw new IllegalArgumentException("No se encontró el concurso con la edición: " + edicion);
        }

        // Verifica si el concurso ya está cerrado
        if (concurso.getFechaFin() != null && concurso.getFechaFin().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("El concurso ya está cerrado.");
        }

        LocalDateTime dateTime = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        // Establece la fecha de cierre del concurso como la fecha y hora actuales
        concurso.setFechaFin(dateTime);

        // Guarda los cambios en el concurso en la base de datos
        concursoRepository.save(concurso);
    }


    /**
     * Genera la edición para un nuevo concurso basándose en el último concurso registrado.
     * Si no hay ningún concurso registrado, se inicia con la edición "III Edicion".
     *
     * @return La edición generada para el nuevo concurso.
     */
    @Override
    @Transactional(readOnly = true)
    public String generateEdicion() {
        // Obtiene el último concurso registrado
        Concurso lastConcurso = getUltimoConcurso();

        // Verifica si no hay ningún concurso registrado
        if (lastConcurso == null) {
            // Si no hay ningún concurso registrado, se inicia con la edición "III Edicion"
            return RomanNumerals.convert(3) + " Edicion";
        } else {
            // Obtiene el número de la última edición
            String lastEdicionNumber = lastConcurso.getEdicion().substring(0, lastConcurso.getEdicion().indexOf(" "));
            // Calcula el número de la siguiente edición sumando 1 al número de la última edición
            int nextEdicionNumber = RomanNumerals.toInteger(lastEdicionNumber) + 1;
            // Genera la edición para el nuevo concurso utilizando números romanos
            return RomanNumerals.convert(nextEdicionNumber) + " Edicion";
        }
    }


    /**
     * Reabre el concurso con la edición especificada y establece una nueva fecha de cierre.
     *
     * @param edicion   La edición del concurso que se va a reabrir.
     * @param fechaFin  La nueva fecha de cierre para el concurso.
     * @throws IllegalArgumentException Si no se encuentra ningún concurso con la edición especificada.
     * @throws IllegalStateException    Si el concurso ya está abierto.
     */
    @Override
    @Transactional
    public void reabrirConcurso(String edicion, LocalDateTime fechaFin) {
        // Obtiene el concurso actual utilizando el método correspondiente.
        Concurso concursoActual = concursoRepository.findCurrentConcurso(LocalDateTime.now());

        // Si hay un concurso actual, lanza una excepción.
        if (concursoActual != null) {
            throw new IllegalStateException("No se puede reabrir un concurso porque ya hay un concurso actual.");
        }

        // Busca el concurso con la edición especificada en la base de datos
        Concurso concurso = concursoRepository.findConcursoByEdicion(edicion);

        // Verifica si se encontró el concurso
        if (concurso == null) {
            throw new IllegalArgumentException("No se encontró el concurso con la edición: " + edicion);
        }

        // Verifica si el concurso ya está abierto
        if (concurso.getFechaFin() == null || concurso.getFechaFin().isAfter(LocalDateTime.now())) {
            throw new IllegalStateException("El concurso ya está abierto.");
        }

        // Establece la nueva fecha de cierre del concurso
        concurso.setFechaFin(fechaFin);

        // Guarda los cambios en el concurso en la base de datos
        concursoRepository.save(concurso);
    }


    /**
     * Actualiza la fecha segun el campo especificado de un concurso obtenido por su edición.
     *
     * @param edicion La edición del concurso cuyas fechas se van a actualizar.
     * @param campo El campo de fecha que se va a actualizar (por ejemplo, "fechaInicio").
     * @param fecha La nueva fecha y hora para el campo especificado.
     * @throws IllegalArgumentException Si se proporciona un campo de fecha no válido o una edición de concurso inválida.
     */
    @Override
    @Transactional
    public void updateFecha(String edicion, String campo, LocalDateTime fecha) {
        // Busca el concurso por su edición en la base de datos
        Concurso concurso = concursoRepository.findConcursoByEdicion(edicion);

        // Verifica si se encontró el concurso
        if (concurso != null) {
            // Actualiza el campo de fecha correspondiente según el valor de "campo"
            switch (campo) {
                case "fechaInicio":
                    concurso.setFechaInicio(fecha);
                    break;
                case "fechaFinPostulaciones":
                    concurso.setFechaFinPostulaciones(fecha);
                    break;
                case "fechaInicioVotacion":
                    concurso.setFechaInicioVotacion(fecha);
                    break;
                case "fechaFinVotacion":
                    concurso.setFechaFinVotacion(fecha);
                    break;
                case "fechaFin":
                    concurso.setFechaFin(fecha);
                    break;
                default:
                    // Si se proporciona un campo de fecha no válido, lanza una excepción
                    throw new IllegalArgumentException("Campo inválido: " + campo);
            }

            // Guarda los cambios en el concurso en la base de datos
            concursoRepository.save(concurso);
        } else {
            // Si no se encuentra el concurso correspondiente a la edición proporcionada, lanza una excepción
            throw new IllegalArgumentException("Edición inválida: " + edicion);
        }
    }

}
