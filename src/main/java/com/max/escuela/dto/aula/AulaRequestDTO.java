package com.max.escuela.dto.aula;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@Schema(description = "Datos para registrar un aula")
public record AulaRequestDTO(
        @Schema(description = "Nombre del aula", example = "Aula 101")
        @NotBlank(message = "El nombre es requerido")
        @Size(min = 5, max = 100, message = "El nombre debe tener entre 5 y 50 caracteres")
        String nombre,

        @Schema(description = "Capacidad del aula", example = "30")
        @Positive(message = "La capacidad debe ser un número positivo")
        Integer capacidad
) { }