package ar.edu.unnoba.appweb_concurso_materiales_educativos.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Table(name="materiales")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Material implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_material", nullable = false)
    private Long id;

    @Column(name = "titulo")
    private String titulo;

    @Column(name = "descripcion")
    private String descripcion;

    @Column(name = "tipo_material")
    private String tipoMaterial;

    @Column(name = "disciplina")
    private String disciplina;

    @Column(name = "autores")
    private String autores;

    @Column(name = "aprobado")
    private Boolean aprobado;

    @Column(name = "evaluado")
    private Boolean evaluado = false;

    @Column(name = "archivo")
    private String archivoPdf;

    @Column(name = "url_video_presentacion")
    private String urlVideoPresentacion;

    @Column(name = "likes")
    private int likes = 0;

    @Column(name = "ganador")
    private boolean ganador = false;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private User concursante;

    @EqualsAndHashCode.Exclude
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "evaluador_material",
        joinColumns = @JoinColumn(name = "material_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id"))
    @JsonManagedReference
    private Set<User> evaluadores = new HashSet<>();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "concurso_id")
    private Concurso concurso;
}
