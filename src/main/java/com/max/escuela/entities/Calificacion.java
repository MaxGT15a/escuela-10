package com.max.escuela.entities;

import com.max.escuela.exceptions.InvalidDataException;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "CALIFICACIONES")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class Calificacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_CALIFICACION")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_INSCRIPCION", nullable = false, unique = true)
    private Inscripcion inscripcion;

    @Column(name = "CALIFICACION")
    private BigDecimal calificacion;

    @Column(name = "FECHA_REGISTRO", nullable = false)
    private LocalDate fechaRegistro;

    private static void validarDatos(
            BigDecimal calificacion
    ){
        if(calificacion == null)
            throw new InvalidDataException("La calificacion es requerida");
        if(
            calificacion.compareTo(BigDecimal.TEN) > 0 ||
            calificacion.compareTo(BigDecimal.ZERO) < 0
        )
            throw new InvalidDataException("La calificacion debe ser positiva y estar entre 0 y 10");
    }

    public void asignarInscripcion(Inscripcion inscripcion) {
        if(inscripcion == null)
            throw new InvalidDataException("La inscripcion es requerida");
        this.inscripcion = inscripcion;
    }

    public void actualizar(
            BigDecimal calificacion
    ){
        validarDatos(calificacion);
        this.calificacion = calificacion;
    }

    public static Calificacion crear(BigDecimal calificacion){
        validarDatos(calificacion);
        return Calificacion.builder()
                .calificacion(calificacion)
                .fechaRegistro(LocalDate.now())
                .build();
    }
}
