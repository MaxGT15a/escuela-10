package com.max.escuela.entities;

import com.max.escuela.utils.StringCustomUtils;
import com.max.escuela.utils.NumCustomUtils;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "CURSOS")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class Curso {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_CURSO")
    private Long id;

    @Column(name = "NOMBRE", length = 100, nullable = false, unique = true)
    private String nombre;

    @Column(name = "DESCRIPCION", length = 200, nullable = false)
    private String descripcion;

    @Column(name = "CREDITOS", nullable = false)
    private Integer creditos;

    private static void validarDatos(
            String nombre,
            Integer creditos
    ){
        StringCustomUtils.validarTamanio(
                nombre, 5, 100,
                "El nombre es requerido y debe tener entre 5 y 100 caracteres"
        );

        NumCustomUtils.validarEnteroPositvo(
                creditos,
                "El credito es requerido y debe ser positivo"
        );
    }

    public void actualizar(
            String nombre,
            String descripcion,
            Integer creditos
    ){
        validarDatos(nombre, creditos);
        this.nombre = nombre.trim();
        this.descripcion = descripcion == null ? null : descripcion.trim();
        this.creditos = creditos;
    }

    public static Curso crear(String nombre, String descripcion, Integer creditos
    ){
        validarDatos(nombre, creditos);
        return Curso.builder()
                .nombre(nombre.trim())
                .descripcion(descripcion == null ? null : descripcion.trim())
                .creditos(creditos)
                .build();
    }
}
