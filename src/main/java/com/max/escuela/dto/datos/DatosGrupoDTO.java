package com.max.escuela.dto.datos;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Datos de un grupo")
public record DatosGrupoDTO(

        @Schema(description = "Nombre del alumno", example = "Juan Pérez López")
        String curso,

        @Schema(description = "Matricula del alumno", example = "A2025001")
        String maestro,

        @Schema(description = "Email del alumno", example = "juan.perez@alumnos.com")
        String aula,

        @Schema(description = "Fecha de ingreso del alumno", example = "10/01/2025")
        String periodo
) { }