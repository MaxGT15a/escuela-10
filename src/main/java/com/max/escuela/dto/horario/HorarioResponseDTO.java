package com.max.escuela.dto.horario;

import com.max.escuela.dto.datos.DatosGrupoDTO;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Datos de un horario")
public record HorarioResponseDTO(
        @Schema(description = "Identificador del horario", example = "1")
        Long id,

        @Schema(description = "Datos del grupo")
        DatosGrupoDTO grupo,

        @Schema(description = "Horario del grupo", example = "Viernes 8:00 a 10:00")
        String horario
) { }