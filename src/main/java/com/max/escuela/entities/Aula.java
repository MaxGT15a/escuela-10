package com.max.escuela.entities;

import com.max.escuela.exceptions.InvalidDataException;
import com.max.escuela.utils.StringCustomUtils;
import com.max.escuela.utils.NumCustomUtils;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class Aula {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_AULA")
    private Long id;

    @Column(name = "NOMBRE", length = 30, nullable = false, unique = true)
    private String nombre;

    @Column(name = "CAPACIDAD", nullable = false)
    private Integer capacidad;

    @Builder.Default
    @OneToMany(mappedBy = "aula", fetch = FetchType.LAZY)
    private List<Grupo> grupos = new ArrayList<>();

    public static void validarDatos(
        String nombre,
        Integer capacidad
    ){
        StringCustomUtils.validarTamanio(nombre, 5, 30,
        "El nombre es requerido y debe tener entre 5 y 30 caracteres"
        );

        NumCustomUtils.validarEnteroPositvo(capacidad,
        "La capacidad es requerida y debe ser positiva"
        );
    }

    public void asignarGrupo(Grupo grupo){
        if(grupo == null)
            throw new InvalidDataException("El grupo es requerido");

        grupo.asignarAula(this);
        this.grupos.add(grupo);
    }

    public void actualizar(
            String nombre,
            Integer capacidad
    ){
        validarDatos(nombre, capacidad);

        this.nombre = nombre.trim();
        this.capacidad = capacidad;
    }

    public void desasignarGrupo(Grupo grupo){
        this.grupos.remove(grupo);
    }

    public static Aula crear(
            String nombre,
            Integer capacidad
    ){
        validarDatos(nombre, capacidad);

        return Aula.builder()
                .nombre(nombre.trim())
                .capacidad(capacidad)
                .build();
    }
}
