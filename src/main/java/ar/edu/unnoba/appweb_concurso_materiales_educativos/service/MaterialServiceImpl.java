package ar.edu.unnoba.appweb_concurso_materiales_educativos.service;

import ar.edu.unnoba.appweb_concurso_materiales_educativos.dto.MaterialDTO;
import ar.edu.unnoba.appweb_concurso_materiales_educativos.model.Concurso;
import ar.edu.unnoba.appweb_concurso_materiales_educativos.model.Evaluacion;
import ar.edu.unnoba.appweb_concurso_materiales_educativos.model.Material;
import ar.edu.unnoba.appweb_concurso_materiales_educativos.model.User;
import ar.edu.unnoba.appweb_concurso_materiales_educativos.repository.ConcursoRepository;
import ar.edu.unnoba.appweb_concurso_materiales_educativos.repository.EvaluacionRepository;
import ar.edu.unnoba.appweb_concurso_materiales_educativos.repository.MaterialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class MaterialServiceImpl implements MaterialService {

    private final MaterialRepository materialRepository;
    private final ConcursoRepository concursoRepository;
    private final EvaluacionRepository evaluacionRepository;

    @Autowired
    public MaterialServiceImpl(MaterialRepository materialRepository, ConcursoRepository concursoRepository, EvaluacionRepository evaluacionRepository) {
        this.materialRepository = materialRepository;
        this.concursoRepository = concursoRepository;
        this.evaluacionRepository = evaluacionRepository;
    }

    /**
     * Crea un nuevo material y lo asocia con un usuario y un concurso.
     *
     * @param material El material que se va a crear y asociar.
     * @param user     El usuario que será el concursante del material.
     * @param concurso El concurso al que pertenecerá el material.
     * @return El material creado y asociado.
     */
    @Override
    @Transactional
    public Material createMaterial(Material material, User user, Concurso concurso) {
        // Establece al usuario como concursante del material.
        material.setConcursante(user);
        // Establece el concurso al que pertenecerá el material.
        material.setConcurso(concurso);

        //Guarda el material en el repositorio de materiales y lo retorna.
        return materialRepository.save(material);
    }


    /**
     * Incrementa el contador de likes de un material y guarda la modificación en el repositorio.
     *
     * @param material El material al que se le dará like.
     */
    @Transactional
    @Override
    public void darLikeMaterial(Material material){
        // Aumenta el contador de likes del material
        material.setLikes(material.getLikes() + 1);
        // Guarda la modificación del material en el repositorio
        materialRepository.save(material);
    }


    /**
     * Obtiene una lista de materiales asociados a un concurso específico.
     *
     * @param concurso El concurso del cual se desean recuperar los materiales.
     * @return Una lista de materiales asociados al concurso especificado.
     */
    @Override
    @Transactional(readOnly = true)
    public List<Material> getMaterialesByConcurso(Concurso concurso) {
        if (concurso == null) {
            return List.of();
        }
        return materialRepository.findAllByConcurso(concurso);
    }


    /**
     * Recupera una lista de materiales asociados a un concursante específico.
     *
     * @param user El concursante para el cual se recuperarán los materiales.
     * @return Una lista de materiales asociados al concursante, o una lista vacía si no hay materiales.
     */
    @Override
    @Transactional(readOnly = true)
    public List<Material> getMaterialesByConcursante(User user) {
        return materialRepository.findAllByConcursante(user);
    }


    /**
     * Recupera una lista de todos los materiales disponibles.
     *
     * @return Una lista de todos los materiales, o una lista vacía si no hay materiales.
     */
    @Override
    @Transactional(readOnly = true)
    public List<Material> getMateriales() {
        return materialRepository.findAll();
    }


    /**
     * Obtiene una lista de materiales participantes en un concurso específico.
     *
     * @param concurso El concurso del cual se desean recuperar los materiales participantes.
     * @return Una lista de materiales participantes asociados al concurso especificado.
     */
    @Transactional(readOnly = true)
    @Override
    public List<Material> getMaterialesParticipantesByConcurso(Concurso concurso) {
        if (concurso == null) {
            return List.of();
        }
        return materialRepository.findMaterialsByAprobadoIsTrueAndConcurso(concurso);
    }


    /**
     * Obtiene una lista de materiales pendientes de aprobación asociados al concurso actual.
     * Los materiales pendientes de aprobación son aquellos que aún no han sido aprobados por los administradores.
     *
     * @return Una lista de objetos MaterialDTO que representan los materiales pendientes de aprobación.
     */
    @Override
    @Transactional(readOnly = true)
    public List<MaterialDTO> getMaterialesPendientesAprobacion() {
        // Obtiene el concurso actual.
        Concurso concurso = concursoRepository.findCurrentConcurso(LocalDateTime.now());

        // Obtiene una lista de materiales sin aprobar asociados al concurso actual.
        List<Material> materialesSinAprobar = materialRepository.findMaterialsByAprobadoIsNullAndConcurso(concurso);

        // Crea una lista para almacenar los materiales DTO.
        List<MaterialDTO> materialesDTO = new ArrayList<>();

        // Itera sobre los materiales sin aprobar para crear los objetos MaterialDTO correspondientes.
        for (Material material : materialesSinAprobar) {
            MaterialDTO dto = new MaterialDTO();
            dto.setId(material.getId());
            dto.setTitulo(material.getTitulo());
            dto.setDescripcion(material.getDescripcion());
            dto.setDisciplina(material.getDisciplina());
            dto.setTipoMaterial(material.getTipoMaterial());
            dto.setUrlVideoPresentacion(material.getUrlVideoPresentacion());
            dto.setConcursante(material.getConcursante());
            dto.setAutores(material.getAutores());

            materialesDTO.add(dto);
        }

        return materialesDTO;
    }


    /**
     * Obtiene una lista de materiales pendientes de evaluación.
     *
     * @return Una lista de materiales pendientes de evaluación.
     */
    @Override
    @Transactional(readOnly = true)
    public List<Material> getMaterialesPendientesEvaluacion() {
        // Obtiene el concurso actual.
        Concurso concurso = concursoRepository.findCurrentConcurso(LocalDateTime.now());

        // Busca los materiales pendientes de evaluación para el concurso actual.
        return materialRepository.findMaterialsByEvaluadoIsFalseAndAprobadoIsTrueAndConcurso(concurso);
    }


    /**
     * Obtiene un material específico por su id.
     *
     * @param id La identificación del material a recuperar.
     * @return El material asociado a la identificación proporcionada, o null si no se encuentra.
     */
    @Override
    @Transactional(readOnly = true)
    public Material getMaterial(Long id) {
        return materialRepository.findMaterialById(id);
    }


    /**
     * Obtiene un conjunto de materiales asignados a un usuario (evaluador) específico.
     *
     * @param user El usuario para el cual se recuperarán los materiales asignados.
     * @return Un conjunto de materiales asignados al usuario.
     */
    @Override
    @Transactional(readOnly = true)
    public Set<Material> getMaterialesAsignados(User user) {
        // Devuelve el conjunto de materiales asignados al usuario
        // accediendo a la propiedad materialesAEvaluar del usuario.
        return user.getMaterialesAEvaluar();
    }


    /**
     * Obtiene una lista de materiales asignados pendientes de evaluación para un evaluador específico.
     *
     * @param userId El ID del evaluador para el cual se desean obtener los materiales asignados pendientes.
     * @return Una lista de materiales asignados pendientes de evaluación para el evaluador dado.
     */
    @Override
    @Transactional(readOnly = true)
    public List<Material> getMaterialesAsignadosPendientes(Long userId) {
        return materialRepository.findMaterialesPendientesEvaluadorById(userId);
    }


    /**
     * Aprueba un material educativo especificado por su id.
     *
     * @param id La identificación del material educativo a aprobar.
     * @throws IllegalArgumentException Si no se encuentra el material educativo con la identificación proporcionada.
     */
    @Override
    @Transactional
    public void aprobarMaterial(Long id) {
        // Utiliza el repositorio de materiales para buscar el material por su identificación.
        Material material = materialRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Material educativo no encontrado con id: " + id));

        // Establece el estado de aprobación del material como verdadero.
        material.setAprobado(true);

        // Guarda los cambios en el repositorio de materiales.
        materialRepository.save(material);
    }


    /**
     * Rechaza un material educativo especificado por su id.
     *
     * @param id La identificación del material educativo a rechazar.
     * @throws IllegalArgumentException Si no se encuentra el material educativo con la identificación proporcionada.
     */
    @Override
    @Transactional
    public void rechazarMaterial(Long id) {
        // Utiliza el repositorio de materiales para buscar el material por su identificación.
        Material material = materialRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Material educativo no encontrado con id: " + id));

        // Establece el estado de aprobación del material como falso.
        material.setAprobado(false);

        // Guarda los cambios en el repositorio de materiales.
        materialRepository.save(material);
    }


    /**
     * Actualiza el estado de evaluación de un material educativo.
     *
     * @param materialId El identificador del material educativo cuyo estado de evaluación se actualizará.
     */
    @Override
    @Transactional
    public void updateEvaluado(Long materialId) {
        // Carga el material desde la base de datos.
        Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new RuntimeException("Material not found"));

        // Inicializa el contador de evaluaciones del material.
        int totalEvaluacionesMaterial = 0;
        // Recorre cada evaluador del material.
        for (User evaluador : material.getEvaluadores()){
            // Busca si el evaluador ha realizado una evaluación para este material.
            Evaluacion evaluacion = evaluacionRepository.findByEvaluadorAndMaterial(evaluador, material);
            // Si se encuentra una evaluación para el evaluador actual, aumenta el contador de evaluaciones.
            if (evaluacion != null) {
                totalEvaluacionesMaterial += 1;
            }
        }
        // Obtiene el total de evaluadores asignados al material.
        int totalEvaluadoresMaterial = material.getEvaluadores().size();

        // Establece el estado de evaluación del material basándose en si el número de evaluaciones es igual al número de evaluadores.
        material.setEvaluado(totalEvaluacionesMaterial == totalEvaluadoresMaterial);

        // Guarda los cambios en el repositorio de materiales.
        materialRepository.save(material);
    }


    /**
     * Establece un material como ganador de un concurso.
     *
     * @param materialId La identificación del material que se establecerá como ganador.
     */
    @Override
    @Transactional
    public void setMaterialGanador(Long materialId){
        // Busca el material en la base de datos utilizando su ID.
        Material material = materialRepository.findMaterialById(materialId);
        // Establece el estado del material como ganador.
        material.setGanador(true);
        // Guarda los cambios en el material en la base de datos.
        materialRepository.save(material);
    }

    /**
     * Método para obtener la lista de materiales ganadores de un concurso
     * @param concurso El concurso del cual se desean recuperar los materiales ganadores.
     * @return Una lista de materiales ganadores asociados al concurso especificado.
     */
    @Override
    @Transactional(readOnly = true)
    public List<Material> getMaterialesGanadores(Concurso concurso){
        if (concurso == null) {
            return List.of();
        }
        return materialRepository.findMaterialesGanadoresByConcurso(concurso);
    }

    /**
     * Método para remover el estado de ganador de un material
     * @param materialId La identificación del material que se removerá como ganador.
     */
    @Override
    @Transactional
    public void removeMaterialGanador(Long materialId) {
        // Busca el material en la base de datos utilizando su ID.
        Material material = materialRepository.findMaterialById(materialId);
        // Quitar el estado del material como ganador.
        material.setGanador(false);
        // Guarda los cambios en el material en la base de datos.
        materialRepository.save(material);
    }
}
