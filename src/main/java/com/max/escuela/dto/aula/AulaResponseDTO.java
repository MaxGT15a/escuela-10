package com.max.escuela.dto.aula;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Datos de una aula")
public record AulaResponseDTO(
        @Schema(description = "Identificador del curso", example = "1")
        Long id,

        @Schema(description = "Nombre del aula", example = "Aula 101")
        String nombre,

        @Schema(description = "Capacidad del aula", example = "30")
        Integer capacidad
) { }