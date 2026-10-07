package com.max.escuela.entities;

import com.max.escuela.exceptions.InvalidDataException;
import com.max.escuela.utils.StringCustomUtils;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "ALUMNOS")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class Alumno {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_ALUMNO")
    private Long id;

    @Column(name = "NOMBRE", length = 50, nullable = false)
    private String nombre;

    @Column(name = "APELLIDO_PATERNO", length = 50, nullable = false)
    private String apellidoPaterno;

    @Column(name = "APELLIDO_MATERNO", length = 50, nullable = false)
    private String apellidoMaterno;

    @Column(name = "EMAIL", length = 100, nullable = false, unique = true)
    private String email;

    @Column(name = "MATRICULA", length = 10, nullable = false, unique = true)
    private String matricula;

    @Builder.Default
    @Column(name = "FECHA_INGRESO", nullable = false)
    private LocalDate fechaIngreso = LocalDate.now();

    @Builder.Default
    @OneToMany(mappedBy = "alumno", fetch = FetchType.LAZY)
    private List<Inscripcion> inscripciones = new ArrayList<>();

    private static void validarDatos(
            String nombre,
            String apellidoPaterno,
            String apellidoMaterno
    ){
        StringCustomUtils.validarTamanio(
                nombre, 5, 50,
                "El nombre es requerido y debe tener entre 5 y 50 caracteres"
        );

        StringCustomUtils.validarTamanio(
                apellidoPaterno, 5, 50,
                "El apellido paterno es requerido y debe tener entre 5 y 50 caracteres"
        );

        StringCustomUtils.validarTamanio(
                apellidoMaterno, 5, 50,
                "El apellido materno es requerido y debe tener entre 5 y 50 caracteres"
        );
    }

    public void agregarInscripcion(Inscripcion inscripcion){
        if (inscripcion == null)
            throw new InvalidDataException("La inscripcion es requerida");
        this.inscripciones.add(inscripcion);
    }

    public void quitarInscripcion(Inscripcion inscripcion){
        if (inscripcion == null)
            throw new InvalidDataException("La inscripcion es requerida");
        this.inscripciones.remove(inscripcion);
    }

    public void asignarDatosAcademicos(String email, String matricula) {
        StringCustomUtils.validarTamanio(
                email, 5, 100,
                "El email es requerido y debe tener entre 5 y 100 caracteres"
        );
        StringCustomUtils.validarTamanio(
                matricula, 5, 10,
                "La matricula es requerida y debe tener entre 5 y 10 caracteres"
        );
        this.email = email.toLowerCase().trim();
        this.matricula = matricula.trim();
    }

    public boolean cambioEnDatos(String nombre, String apellidoPaterno, String apellidoMaterno){
        validarDatos(nombre, apellidoPaterno, apellidoMaterno);
        return !this.nombre.equals(nombre.trim()) ||
                !this.apellidoPaterno.equals(apellidoPaterno.trim()) ||
                !this.apellidoMaterno.equals(apellidoMaterno.trim());
    }

    public void actualizar(
            String nombre,
            String apellidoPaterno,
            String apellidoMaterno,
            String email,
            String matricula
    ){
        asignarDatosAcademicos(email, matricula);
        this.nombre = nombre.trim();
        this.apellidoPaterno = apellidoPaterno.trim();
        this.apellidoMaterno = apellidoMaterno.trim();
    }


    /**
     * Solo promedia calificaciones no null. Si no hay ninguna, devuelve 0.00.
     */
    public BigDecimal calcularPromedio() {
        List<BigDecimal> notas = inscripciones.stream()
                .map(Inscripcion::getCalificacion)
                .filter(Objects::nonNull)
                .map(Calificacion::getCalificacion)
                .filter(Objects::nonNull)
                .toList();

        if (notas.isEmpty())
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);


        return notas.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(notas.size()), 2, RoundingMode.HALF_UP);
    }

    public String obtenerNombreCompleto() {
        return String.format("%s %s %s", nombre, apellidoPaterno, apellidoMaterno);
    }

    public static Alumno crear(
            String nombre,
            String apellidoPaterno,
            String apellidoMaterno
            // Matricula se genera en la base de datos
    ){
        validarDatos(nombre, apellidoPaterno, apellidoMaterno);

        return Alumno.builder()
                .nombre(nombre.trim())
                .apellidoPaterno(apellidoPaterno.trim())
                .apellidoMaterno(apellidoMaterno.trim())
                .build();
    }
}
