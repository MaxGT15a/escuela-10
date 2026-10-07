package com.max.escuela.dto.datos;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Datos de un aula")
public record DatosAulaDTO(
        @Schema(description = "Nombre del aula", example = "Aula 101")
        String nombre,

        @Schema(description = "Capacidad del aula", example = "30")
        Integer capacidad
) { }