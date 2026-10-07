package com.max.escuela.entities;


import com.max.escuela.exceptions.InvalidDataException;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(
        name = "INSCRIPCIONES",
        uniqueConstraints = {
                @UniqueConstraint(name = "INSCRIPCION_ALU_GRU_UK", columnNames = {
                        "ID_ALUMNO", "ID_GRUPO"
                })
        }
)
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class Inscripcion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_INSCRIPCION")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_ALUMNO", nullable = false)
    private Alumno alumno;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_GRUPO", nullable = false)
    private Grupo grupo;

    @Column(name = "FECHA_INSCRIPCION", nullable = false)
    private LocalDate fechaInscripcion;

    @OneToOne(mappedBy = "inscripcion")
    private Calificacion calificacion;

    public void asignarAlumno(Alumno alumno){
        if(alumno == null)
            throw new InvalidDataException("El alumno es requerido");

        alumno.agregarInscripcion(this);
        this.alumno = alumno;
    }


    public void asignarGrupo(Grupo grupo){
        if(grupo == null)
            throw new InvalidDataException("El grupo es requerido");
        grupo.agregarInscripcion(this);
        this.grupo = grupo;
    }

    public void validarDatos(
            Alumno alumno,
            Grupo grupo
    ){
        if(alumno == null)
            throw new InvalidDataException("El alumno es requerido");
        if(grupo == null)
            throw new InvalidDataException("El grupo es requerido");
    }

    public boolean cambioEnDatos(
            Alumno alumno,
            Grupo grupo
    ){
        validarDatos(alumno, grupo);
        return !this.alumno.equals(alumno) || !this.grupo.equals(grupo);
    }

    public void actualizar(
            Alumno alumno,
            Grupo grupo
    ){
        validarDatos(alumno, grupo);
        this.alumno.quitarInscripcion(this);

        this.alumno = alumno;
        this.alumno.agregarInscripcion(this);

        this.grupo.quitarInscripcion(this);
        this.grupo = grupo;
        this.grupo.agregarInscripcion(this);
    }

    public static Inscripcion crear(){
        return Inscripcion.builder()
                .fechaInscripcion(LocalDate.now())
                .build();
    }
}
