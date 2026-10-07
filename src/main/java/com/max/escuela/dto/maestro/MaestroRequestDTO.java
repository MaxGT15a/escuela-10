package com.max.escuela.dto.maestro;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Datos para registrar un maestro")
public record MaestroRequestDTO(
    @Schema(description = "Nombre del maestro", example = "Laura")
    @NotBlank(message = "El nombre es requerido")
    @Size(min = 5, max = 50, message = "El nombre debe tener entre 5 y 50 caracteres")
    String nombre,

    @Schema(description = "Apellido paterno del maestro", example = "Martínez")
    @NotBlank(message = "El apellido paterno es requerido")
    @Size(min = 5, max = 50, message = "El apellido paterno debe tener entre 5 y 50 caracteres")
    String apellidoPaterno,

    @Schema(description = "Apellido materno del maestro", example = "Martínez")
    @NotBlank(message = "El apellido materno es requerido")
    @Size(min = 5, max = 50, message = "El apellido materno debe tener entre 5 y 50 caracteres")
    String apellidoMaterno,

    @Schema(description = "Email del maestro", example = "laura.martinez@escuela.com")
    @NotBlank(message = "El email es requerido")
    @Size(min = 5, max = 100, message = "El email debe tener entre 5 y 100 caracteres")
    @Email(message = "El email debe tener un formato valido (ejemplo@dominio.com)")
    String email,

    @Schema(description = "Numero telefonico del maestro", example = "5551010789")
    @NotBlank(message = "El numero telefonico es requerido")
    @Pattern(regexp = "^[0-9]{10}", message = "El telefono debe contener exactamente 10 digitos")
    String telefono
) { }