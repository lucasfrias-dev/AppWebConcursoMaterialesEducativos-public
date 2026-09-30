package ar.edu.unnoba.appweb_concurso_materiales_educativos.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.*;

@Data
@Entity
@Table(name="usuarios")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario", nullable = false)
    private Long id;

    @Column(name="email", nullable = false, unique = true)
    private String email;

    @Column(name ="password", nullable = false)
    private String password;

    @Column(name = "nombre")
    private String nombre;

    @Column(name = "apellido")
    private String apellido;

    @Column(name = "rol")
    @Enumerated(EnumType.STRING)
    private Rol rol;

    @Column(name = "active")
    private boolean active = true;

    @EqualsAndHashCode.Exclude
    @ManyToMany(mappedBy = "evaluadores", fetch = FetchType.LAZY)
    @JsonBackReference
    private Set<Material> materialesAEvaluar = new HashSet<>();

    @Override
    public String toString() {
        return "id=" + id +
                ", email='" + email + '\'' +
                ", nombre='" + nombre + '\'' +
                ", apellido='" + apellido + '\'';
    }

    // Rol es un enum que define los roles de los usuarios
    public enum Rol {
        ADMINISTRADOR,
        EVALUADOR,
        CONCURSANTE
    }

}

