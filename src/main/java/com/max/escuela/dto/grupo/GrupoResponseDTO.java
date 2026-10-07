package com.max.escuela.dto.grupo;

import com.max.escuela.dto.datos.DatosAulaDTO;
import com.max.escuela.dto.datos.DatosCursoDTO;
import com.max.escuela.dto.datos.DatosMaestroDTO;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Datos de un grupo")
public record GrupoResponseDTO(
        @Schema(description = "Identificador del grupo", example = "1")
        Long id,

        @Schema(description = "Datos del curso")
        DatosCursoDTO curso,

        @Schema(description = "Datos del maestro")
        DatosMaestroDTO maestro,

        @Schema(description = "Datos del aula")
        DatosAulaDTO aula,

        @Schema(description = "Horarios del grupo", example = "[\"Lunes 10:00 - 12:00\", \"Miércoles 14:00 - 16:00\"]")
        List<String> horarios,

        @Schema(description = "Fecha de ingreso del alumno", example = "10/01/2025")
        String periodo
) { }