package com.max.escuela.dto.alumno;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Datos para registrar un alumno")
public record AlumnoRequestDTO(
        @Schema(description = "Nombre del alumno", example = "Carlos")
        @NotBlank(message = "El nombre es requerido")
        @Size(min = 5, max = 50, message = "El nombre debe tener entre 5 y 50 caracteres")
        String nombre,

        @Schema(description = "Apellido paterno del maestro", example = "González")
        @NotBlank(message = "El apellido paterno es requerido")
        @Size(min = 5, max = 50, message = "El apellido paterno debe tener entre 5 y 50 caracteres")
        String apellidoPaterno,

        @Schema(description = "Apellido materno del maestro", example = "Ramírez")
        @NotBlank(message = "El apellido materno es requerido")
        @Size(min = 5, max = 50, message = "El apellido materno debe tener entre 5 y 50 caracteres")
        String apellidoMaterno
) { }