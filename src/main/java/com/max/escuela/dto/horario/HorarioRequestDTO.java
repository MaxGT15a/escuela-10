package com.max.escuela.dto.horario;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

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
        @NotBlank(message = "La hora de inicio es requerida")
        @NotNull(message = "La hora de inicio es requerida")
        @Size(min = 5, max = 5, message = "La hora de inicio debe tener el formato HH:mm")
        String horaInicio,

        @Schema(description = "Hora de fin del horario", example = "10:00")
        @NotBlank(message = "La hora de fin es requerida")
        @NotNull(message = "La hora de fin es requerida")
        @Size(min = 5, max = 5, message = "La hora de fin debe tener el formato HH:mm")
        String horaFin
) { }