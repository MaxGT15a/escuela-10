package com.max.escuela.dto.datos;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Datos de una calificacion de un curso")
public record DatosCalificacionDTO(
        @Schema(description = "Nombre del curso", example = "Matemáticas I")
        String curso,

        @Schema(description = "Periodo del curso", example = "2025-1")
        String periodo,

        @Schema(description = "Calificacion del curso", example = "8")
        BigDecimal calificacion
) { }