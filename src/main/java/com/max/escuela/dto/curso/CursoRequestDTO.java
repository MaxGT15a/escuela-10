package com.max.escuela.dto.curso;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@Schema(description = "Datos para registrar un curso")
public record CursoRequestDTO(
        @Schema(description = "Nombre del curso", example = "Matemáticas I")
        @NotBlank(message = "El nombre es requerido")
        @Size(min = 5, max = 100, message = "El nombre debe tener entre 5 y 100 caracteres")
        String nombre,

        @Schema(description = "Descripcion del curso", example = "Fundamentos matemáticos para nivel básico")
        @Size(max = 200, message = "La descripcion no debe tener mas de 200 caracteres")
        String descripcion,

        @Schema(description = "Creditos del curso", example = "6")
        @Positive(message = "El credito debe ser un número positivo")
        Integer creditos
) { }