package com.max.escuela.dto.inscripcion;

import com.max.escuela.dto.datos.DatosAlumnoDTO;
import com.max.escuela.dto.datos.DatosGrupoDTO;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Datos de una inscripcion de un curso")
public record InscripcionResponseDTO(
        @Schema(description = "Identificador de la inscripcion", example = "1")
        Long id,

        @Schema(description = "Datos del alumno")
        DatosAlumnoDTO alumno,

        @Schema(description = "Datos del grupo")
        DatosGrupoDTO grupo,

        @Schema(description = "Calificacion del curso", example = "8")
        BigDecimal calificacion,

        @Schema(description = "Fecha de inscripcion del curso", example = "11/02/2026")
        String fechaInscripcion
){ }