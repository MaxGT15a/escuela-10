package com.max.escuela.dto.datos;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Datos de una inscripcion de un curso")
public record DatosInscripcionDTO(
        @Schema(description = "Datos del alumno", example = "Juan Pérez López")
        DatosAlumnoDTO alumno,

        @Schema(description = "Datos del grupo", example = "Matemáticas I")
        DatosGrupoDTO grupo,

        @Schema(description = "Fecha de inscripcion del curso", example = "10/01/2025")
        String fechaInscripcion
) { }