package com.max.escuela.dto.curso;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Datos de un curso")
public record CursoResponseDTO(
    @Schema(description = "Identificador del curso", example = "1")
    Long id,

    @Schema(description = "Nombre del curso", example = "Matemáticas I")
    String nombre,

    @Schema(description = "Descripcion del curso", example = "Fundamentos matemáticos para nivel básico")
    String descripcion,

    @Schema(description = "Creditos del curso", example = "6")
    Integer creditos
) { }
