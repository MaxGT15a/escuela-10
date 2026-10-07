package com.max.escuela.dto.grupo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(description = "Datos de un grupo")
public record GrupoRequestDTO(
        @Schema(description = "Identificador del curso", example = "1")
        @NotNull(message = "El identificador del curso es requerido")
        @Positive(message = "El identificador del curso debe ser positivo")
        Long idCurso,

        @Schema(description = "Identificador del maestro", example = "1")
        @NotNull(message = "El identificador del maestro es requerido")
        @Positive(message = "El identificador del maestro debe ser positivo")
        Long idMaestro,

        @Schema(description = "Identificador del aula", example = "1")
        @NotNull(message = "El identificador del aula es requerido")
        @Positive(message = "El identificador del aula debe ser positivo")
        Long idAula,

        @Schema(description = "Periodo del grupo", example = "2025-01")
        @NotBlank(message = "El periodo es requerido")
        @Pattern(regexp = "^\\d{4}-\\d{2}$", message = "El periodo debe tener el formato YYYY-MM")
        @Size(min = 6, max = 20, message = "El periodo debe tener entre 6 y 20 caracteres")
        String periodo
) { }