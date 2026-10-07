package com.max.escuela.dto.calificacion;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

@Schema(description = "Datos para registrar una calificación")
public record CalificacionRequestDTO(
        @Schema(description = "Identificador de la calificacion", example = "1")
        @NotNull(message = "El nombre es requerido")
        @Positive(message = "El identificador debe ser positivo")
        Long idInscripcion,

        @Schema(description = "Calificacion del curso", example = "9.0")
        @NotNull(message = "La calificación es requerida")
        @Positive(message = "La calificacion debe ser positiva")
        BigDecimal calificacion
) { }