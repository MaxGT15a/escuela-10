package com.max.escuela.dto.horario;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(description = "Datos para registrar un horario")
public record HorarioRequestDTO(
        @Schema(description = "Identificador del grupo", example = "1")
        @NotNull(message = "El identificador del grupo es requerido")
        @Positive(message = "El identificador del grupo debe ser positivo")
        Long idGrupo,

        @Schema(description = "Dia del horario", example = "Lunes")
        @NotBlank(message = "El dia es requerido")
        @NotNull(message = "El dia es requerido")
        @Size( max = 15, message = "El dia debe tener como maximo 15 caracteres")
        String dia,

        @Schema(description = "Hora de inicio del horario", example = "08:00")
        @NotNull(message = "La hora de inicio es requerida")
        @Pattern(regexp = "^(?:[01]\\d|2[0-3]):[0-5]\\d$", message = "La hora de inicio debe tener el formato HH:mm válido (00:00 - 23:59)")
        String horaInicio,

        @Schema(description = "Hora de fin del horario", example = "10:00")
        @NotNull(message = "La hora de fin es requerida")
        @Pattern(regexp = "^(?:[01]\\d|2[0-3]):[0-5]\\d$", message = "La hora de fin debe tener el formato HH:mm válido (00:00 - 23:59)")
        String horaFin
) { }