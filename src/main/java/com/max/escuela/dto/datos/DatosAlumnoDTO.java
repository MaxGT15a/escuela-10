package com.max.escuela.dto.datos;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Datos de un alumno")
public record DatosAlumnoDTO(

        @Schema(description = "Nombre del alumno", example = "Juan Pérez López")
        String nombre,

        @Schema(description = "Matricula del alumno", example = "A2025001")
        String matricula,

        @Schema(description = "Email del alumno", example = "juan.perez@alumnos.com")
        String email,

        @Schema(description = "Fecha de ingreso del alumno", example = "10/01/2025")
        String fechaIngreso
) { }