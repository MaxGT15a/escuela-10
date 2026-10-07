package com.max.escuela.dto.inscripcion;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "Datos para registrar una inscripcion")
public record InscripcionRequestDTO(

    @Schema(description = "Identificador del alumno", example = "1")
    @NotNull(message = "El identificador del alumno es requerido")
    @Positive(message = "El identificador del alumno debe ser positivo")
    Long idAlumno,

    @Schema(description = "Identificador del grupo", example = "1")
    @NotNull(message = "El identificador del grupo es requerido")
    @Positive(message = "El identificador del grupo debe ser positivo")
    Long idGrupo
){ }